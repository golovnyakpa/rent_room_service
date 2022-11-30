package my.meetings_room_renter
package logging

import zhttp.http._
import zio._

object VerboseLogging {

  private val headersNotToLog = List("content-length", "host", "content-type")

  def log[R, E >: Throwable]: Middleware[R, E, Request, Response, Request, Response] =
    new Middleware[R, E, Request, Response, Request, Response] {

      override def apply[R1 <: R, E1 >: E](http: Http[R1, E1, Request, Response]): Http[R1, E1, Request, Response] =
        http
          .contramapZIO[R1, E1, Request] { r =>
            for {
              _ <- Console.printLine(s">>>>>> ${r.method} ${r.path} ${r.version}")
              _ <- ZIO.foreach(r.headers.toList) { h =>
                     ZIO.when(!headersNotToLog.contains(h._1))(Console.printLine(s">>> ${h._1}: ${h._2}"))
                   }
              _ <- r.body.asString.flatMap(b => Console.printLine(b))
            } yield r
          }
          .mapZIO[R1, E1, Response] { r =>
            for {
              _ <- Console.printLine(s"<<<<<< Status: ${r.status}")
              _ <- ZIO.foreach(r.headers.toList) { h =>
                     ZIO.when(!headersNotToLog.contains(h._1))(Console.printLine(s"<<< ${h._1}: ${h._2}"))
                   }
              _ <- r.body.asString.flatMap(b => Console.printLine(b) *> Console.printLine("\n"))
            } yield r
          }
    }

}

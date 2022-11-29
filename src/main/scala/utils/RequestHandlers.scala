package my.meetings_room_renter
package utils

import zhttp.http.Request
import zio.json.{DecoderOps, JsonDecoder}
import zio.{IO, ZIO}

object RequestHandlers {

  def parseRequest[T: JsonDecoder](req: Request): IO[String, T] =
    (for {
      body <- req.body.asString
      res  <- ZIO.fromEither(body.fromJson[T])
    } yield res).mapError(err => err.toString)

}

package my.meetings_room_renter
package utils

import zhttp.http.Request
import zio.ZIO
import zio.json.{DecoderOps, JsonDecoder}

object RequestHandlers {

  def parseRequest[T: JsonDecoder](req: Request): ZIO[Any, Serializable, T] =
    for {
      body <- req.body.asString
      res  <- ZIO.fromEither(body.fromJson[T])
    } yield res

}

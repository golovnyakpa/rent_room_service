package my.meetings_room_renter
package api

import my.meetings_room_renter.dao.entities.Room
import zhttp.http.Request
import zio._
import zio.json._
import zio.json.DecoderOps
import my.meetings_room_renter.serde._

object RequestHandlers {

  def handleAddRoomResponse(req: Request): ZIO[Any, Serializable, Room] =
    for {
      body <- req.body.asString
      res <- ZIO.fromEither(body.fromJson[Room])
    } yield res

}

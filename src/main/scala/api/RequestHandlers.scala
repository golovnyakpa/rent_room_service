package my.meetings_room_renter
package api

import my.meetings_room_renter.dao.entities._
import zhttp.http.Request
import zio._
import zio.json._
import zio.json.DecoderOps
import my.meetings_room_renter.serde._

object RequestHandlers {

  def parseRequest[T : JsonDecoder](req: Request): ZIO[Any, Serializable, T] =
    for {
      body <- req.body.asString
      res <- ZIO.fromEither(body.fromJson[T])
      _ <- Console.printLine(res)
    } yield res

//
//  def handleAddRoomResponse(req: Request): ZIO[Any, Serializable, Room] =
//    for {
//      body <- req.body.asString
//      res <- ZIO.fromEither(body.fromJson[Room])
//    } yield res
//
//  def handleAddRentResponse(req: Request): ZIO[Any, Serializable, Rent] =
//    for {
//      body <- req.body.asString
//      res <- ZIO.fromEither(body.fromJson[Rent])
//    } yield res

}

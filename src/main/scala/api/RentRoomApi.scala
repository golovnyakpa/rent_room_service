package my.meetings_room_renter
package api

import my.meetings_room_renter.api.RequestHandlers.handleAddRoomResponse
import my.meetings_room_renter.dao.repositories.RoomRepository
import my.meetings_room_renter.serde._
import my.meetings_room_renter.services.RentRoom
import zhttp.http._
import zio._
import zio.json._

import java.sql.SQLException
import javax.sql.DataSource

object RentRoomApi {

  def roomApi(authed: Ref[List[String]]) = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "rooms" =>
      handleAddRoomResponse(req).foldZIO(
        err => ZIO.succeed(Response.text(err.toString).setStatus(Status.BadRequest)),
        newRoom =>
          RentRoom.RentRoomService
            .addNewRoom(newRoom)
            .flatMap(isInserted =>
              if (isInserted) ZIO.succeed(Response.text(s"${newRoom.toJson} inserted"))
              else ZIO.succeed(Response.text("Such room already exists").setStatus(Status.BadRequest))
            )
      )
  }

  def rentRoomApi(authedUsers: Ref[List[String]]) =
    roomApi(authedUsers) @@ Middleware.debug
}

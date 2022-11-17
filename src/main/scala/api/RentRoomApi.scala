package my.meetings_room_renter
package api

import my.meetings_room_renter.dao.entities.{Rent, Room, UpdatedRent}
import my.meetings_room_renter.dao.repositories.RoomRepository
import my.meetings_room_renter.serde._
import my.meetings_room_renter.services.RentRoom.RentRoomService
import my.meetings_room_renter.utils.RequestHandlers.parseRequest
import my.meetings_room_renter.utils.ResponseMakers
import zhttp.http._
import zio._

import java.sql.SQLException
import javax.sql.DataSource

object RentRoomApi {

  def roomApi(authed: Ref[List[String]]) = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "rooms" =>
      parseRequest[Room](req).foldZIO(
        err => ZIO.succeed(Response.text(err.toString).setStatus(Status.BadRequest)),
        newRoom => ResponseMakers.addNewRoomIfNotExists(newRoom)
      )
    case Method.GET -> Path.root / "rooms" =>
      RentRoomService.listAllRooms.flatMap(lst => ZIO.succeed(Response.text(lst.mkString(", "))))
    case req @ Method.POST -> Path.root / "rents" =>
      parseRequest[Rent](req).foldZIO(
        err => ZIO.succeed(Response.text(err.toString).setStatus(Status.BadRequest)),
        newRent => ResponseMakers.addNewRentIfPossible(newRent)
      )
    case Method.GET -> Path.root / "rents" =>
      RentRoomService.listFutureRents.flatMap(lst => ZIO.succeed(Response.text(lst.mkString(", "))))
    case req @ Method.PUT -> Path.root / "rents" =>
      parseRequest[UpdatedRent](req).foldZIO(
        err => ZIO.succeed(Response.text(err.toString).setStatus(Status.BadRequest)),
        updatedRent => ResponseMakers.updateRentIfPossible(updatedRent)
      )
    case req @ Method.DELETE -> Path.root / "rents" =>
      parseRequest[Rent](req).foldZIO(
        err => ZIO.succeed(Response.text(err.toString).setStatus(Status.BadRequest)),
        rent => ResponseMakers.deleteRent(rent)
      )
  }

  def rentRoomApi(authedUsers: Ref[List[String]]) =
    roomApi(authedUsers)
}

package my.meetings_room_renter
package api

import my.meetings_room_renter.utils.RequestHandlers.parseRequest
import my.meetings_room_renter.dao.entities.{Rent, Room}
import my.meetings_room_renter.dao.repositories.RoomRepository
import my.meetings_room_renter.serde._
import my.meetings_room_renter.services.RentRoom.RentRoomService
import zhttp.http._
import zio._

import java.sql.SQLException
import javax.sql.DataSource

object RentRoomApi {

  private def addNewRoomIfNotExists(
    newRoom: Room
  ): ZIO[DataSource with RoomRepository.RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService
      .addNewRoom(newRoom)
      .flatMap(isInserted =>
        if (isInserted) ZIO.succeed(Response.text(s"${newRoom.roomNumber} inserted"))
        else ZIO.succeed(Response.text("Such room already exists").setStatus(Status.BadRequest))
      )

  private def addNewRentIfPossible(
    newRent: Rent
  ): ZIO[DataSource with RoomRepository.RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService
      .rentRoom(newRent)
      .flatMap {
        case Left(value)  => ZIO.succeed(Response.text(value))
        case Right(value) => ZIO.succeed(Response.text(value).setStatus(Status.BadRequest))
      }

  def roomApi(authed: Ref[List[String]]) = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "rooms" =>
      parseRequest[Room](req).foldZIO(
        err => ZIO.succeed(Response.text(err.toString).setStatus(Status.BadRequest)),
        newRoom => addNewRoomIfNotExists(newRoom)
      )
    case Method.GET -> Path.root / "rooms" =>
      RentRoomService.listAllRooms.flatMap(lst => ZIO.succeed(Response.text(lst.mkString(", "))))
    case req @ Method.POST -> Path.root / "rents" =>
      parseRequest[Rent](req).foldZIO(
        err => ZIO.succeed(Response.text(err.toString).setStatus(Status.BadRequest)),
        newRent => addNewRentIfPossible(newRent)
      )
    case Method.GET -> Path.root / "rents" =>
      RentRoomService.listFutureRents.flatMap(lst => ZIO.succeed(Response.text(lst.mkString(", "))))
  }

  def rentRoomApi(authedUsers: Ref[List[String]]) =
    roomApi(authedUsers)
}

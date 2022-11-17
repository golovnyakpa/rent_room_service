package my.meetings_room_renter
package utils

import my.meetings_room_renter.dao.entities.{Rent, Room, UpdatedRent}
import my.meetings_room_renter.dao.repositories.RoomRepository
import my.meetings_room_renter.services.RentRoom.RentRoomService
import zhttp.http.{Response, Status}
import zio.ZIO

import java.sql.SQLException
import javax.sql.DataSource

object ResponseMakers {
  def addNewRoomIfNotExists(
    newRoom: Room
  ): ZIO[DataSource with RoomRepository.RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService
      .addNewRoom(newRoom)
      .map(isInserted =>
        if (isInserted) Response.text(s"${newRoom.roomNumber} inserted") // todo refactor to correct code
        else Response.text("Such room already exists").setStatus(Status.BadRequest)
      )

  def addNewRentIfPossible(
    newRent: Rent
  ): ZIO[DataSource with RoomRepository.RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService
      .rentRoom(newRent)
      .map {
        case Left(value)  => Response.text(value).setStatus(Status.BadRequest)
        case Right(value) => Response.text(value)
      }

  def updateRentIfPossible(
    updatedRent: UpdatedRent
  ): ZIO[DataSource with RoomRepository.RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService.updateRent(updatedRent).map {
      case Left(value) => Response.text(value)
      case Right(value) =>
        value match {
          case 1 => Response.text("Successfully updated")
          case 0 =>
            Response
              .text("Your rent is not found. Such room doesn't exists or wasn't rent for this tome")
              .setStatus(Status.NotFound)
          case r @ _ => Response.text(s"$r rows were updated. That's wired")
        }
    }

  def deleteRent(
    rent: Rent
  ): ZIO[DataSource with RoomRepository.RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService.deleteRent(rent).map {
      case 0 =>
        Response
          .text("Such rent wasn't found. Check if you entered rent information correctly")
          .setStatus(Status.NotFound)
      case 1     => Response.text(s"Rent for room ${rent.room} was deleted")
      case r @ _ => Response.text(s"$r rows were updated. That's wired").setStatus(Status.NotFound)
    }
}

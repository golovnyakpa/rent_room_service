package my.meetings_room_renter
package utils

import my.meetings_room_renter.authentication.{extractLoginFromJwt, hashPassword}
import my.meetings_room_renter.dao.entities._
import my.meetings_room_renter.dao.repositories.{RentRepositoryService, UserRepository}
import my.meetings_room_renter.services.RentRoomService
import zhttp.http.{Request, Response, Status}
import zio._

import java.sql.SQLException
import javax.sql.DataSource

object ResponseMakers {

  def addNewRoomIfNotExists(
    newRoom: Room
  ): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService
      .addNewRoom(newRoom)
      .map(isInserted =>
        if (isInserted) Response.text(s"${newRoom.roomNumber} inserted").setStatus(Status.Created)
        else Response.text("Such room already exists").setStatus(Status.Conflict)
      )

  def addNewRentIfPossible(
    newRent: Rent
  ): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService
      .rentRoom(newRent)
      .map {
        case Left(value) =>
          Response.text(value).setStatus(Status.Conflict)
        case Right(value) => Response.text(value).setStatus(Status.Created)
      }

  def updateRentIfPossible(
    updatedRent: UpdatedRent,
    req: Request
  ): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Response] = {
    val login: String = extractLoginFromJwt(req.headers.bearerToken)
    RentRoomService.updateRent(updatedRent.copy(renter = Some(login))).map {
      case Left(value) => Response.text(value)
      case Right(value) =>
        value match {
          case 1 => Response.text("Successfully updated")
          case 0 =>
            Response
              .text("Your rent is not found. Such room doesn't exists or wasn't rent for this time")
              .setStatus(Status.NotFound)
          case r @ _ => Response.text(s"$r rows were updated. That's wired")
        }
    }
  }

  def deleteRent(
    rent: Rent
  ): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Response] =
    RentRoomService.deleteRent(rent).map { // todo handle sql exception required
      case 0 =>
        Response
          .text("Such rent wasn't found. Check if you entered rent information correctly")
          .setStatus(Status.NotFound)
      case 1     => Response.text(s"Rent ${rent.dttmStart} - ${rent.dttmEnd} for room ${rent.room} was deleted")
      case r @ _ => Response.text(s"$r rows were updated. That's wired")
    }

  def registerNewUser(user: User): URIO[DataSource with UserRepository, Response] =
    (for {
      repo   <- ZIO.service[UserRepository]
      userId <- repo.registerNewUser(UserDb(user.login, hashPassword(user.password)))
    } yield userId).fold(err => Response.text(s"Error occurred $err"), id => Response.text(s"New user id: $id"))

  def badRequestNotification(err: String): Task[Response] =
    ZIO.succeed(Response.text(s"Something wrong. Details: $err").setStatus(Status.BadRequest))

}

package my.meetings_room_renter
package services

import my.meetings_room_renter.configuration.sqlStateToTextMapping
import my.meetings_room_renter.dao.entities.{Rent, Room, UpdatedRent}
import my.meetings_room_renter.dao.repositories.RentRepositoryService
import my.meetings_room_renter.utils.MessagesGenerator.makeSuccessfulRentNotification
import zio._

import java.sql.SQLException
import javax.sql.DataSource

trait RentRoomService {
  def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Boolean]
  def listAllRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]]
  def rentRoom(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Either[String, String]]
  def listFutureRents(): ZIO[DataSource with RentRepositoryService, SQLException, List[Rent]]
  def updateRent(
    updatedRent: UpdatedRent
  ): ZIO[DataSource with RentRepositoryService, SQLException, Either[String, Long]]
  def deleteRent(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Long]
  def listFutureRentsForUser(user: String): ZIO[DataSource with RentRepositoryService, SQLException, List[Rent]]
}

object RentRoomService {
  def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Boolean] =
    ZIO.serviceWithZIO[RentRoomService](_.addNewRoom(room))

  def listAllRooms: ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, List[Room]] =
    ZIO.serviceWithZIO[RentRoomService](_.listAllRooms())

  def rentRoom(
    rent: Rent
  ): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Either[String, String]] =
    ZIO.serviceWithZIO[RentRoomService](_.rentRoom(rent))

  def listFutureRents: ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, List[Rent]] =
    ZIO.serviceWithZIO[RentRoomService](_.listFutureRents())

  def listFutureRentsForUser(user: String) =
    ZIO.serviceWithZIO[RentRoomService](_.listFutureRentsForUser(user))

  def updateRent(
    updatedRent: UpdatedRent
  ): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Either[String, Long]] =
    ZIO.serviceWithZIO[RentRoomService](_.updateRent(updatedRent))

  def deleteRent(rent: Rent): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Long] =
    ZIO.serviceWithZIO[RentRoomService](_.deleteRent(rent))
}

class RentRoomServiceImpl extends RentRoomService {
  override def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Boolean] =
    RentRepositoryService.getRoom(room).flatMap { opt =>
      if (opt.isEmpty) RentRepositoryService.insertRoom(room).zipRight(ZIO.succeed(true)) // todo refactor it
      else ZIO.succeed(false)
    }

  // in such simple case may be it's better to use RentRepositoryService in the api
  // directly without this proxy service
  override def listAllRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]] =
    RentRepositoryService.listRooms()

  override def listFutureRents(): ZIO[DataSource with RentRepositoryService, SQLException, List[Rent]] =
    RentRepositoryService.listFutureRents()

  def listFutureRentsForUser(user: String): ZIO[DataSource with RentRepositoryService, SQLException, List[Rent]] =
    RentRepositoryService.listFutureRentsForUser(user)

  override def deleteRent(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Long] =
    RentRepositoryService.deleteRent(rent)

  def rentRoom(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Either[String, String]] =
    RentRepositoryService
      .getRoomFromFutureRentsInInterval(rent)
      .flatMap(opt =>
        if (opt.isEmpty) {
          RentRepositoryService
            .insertRent(rent)
            .fold(
              e => Left(s"${sqlStateToTextMapping.getOrElse(e.getSQLState, "Unknown error")}"),
              _ => Right(makeSuccessfulRentNotification(rent))
            )
        } else {
          ZIO.succeed(Left("Rent failed. This time was already booked."))
        }
      )

  def updateRent(
    updatedRent: UpdatedRent
  ): ZIO[DataSource with RentRepositoryService, SQLException, Either[String, Long]] = {
    val newRent =
      Rent(updatedRent.oldRent.room, updatedRent.dttmStart, updatedRent.dttmEnd, updatedRent.oldRent.renter)

    // todo transactional here
    RentRepositoryService.getRoomFromFutureRentsInInterval(newRent).flatMap { opt =>
      if (opt.isEmpty) {
        RentRepositoryService
          .updateRent(updatedRent.oldRent, newRent)
          .fold(
            e => Left(s"${sqlStateToTextMapping.getOrElse(e.getSQLState, e.getMessage)}"),
            res => Right(res)
          )
      } else {
        ZIO.succeed(Left("This time is not available for this room")) // todo think about this (HTTP codes spec)
      }
    }
  }
}

object RentRoomServiceImpl {
  val live: ULayer[RentRoomServiceImpl] = ZLayer.succeed(new RentRoomServiceImpl)
}

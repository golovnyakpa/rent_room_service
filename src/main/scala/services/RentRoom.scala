package my.meetings_room_renter
package services

import my.meetings_room_renter.configuration.sqlStateToTextMapping
import my.meetings_room_renter.dao.entities.{Rent, Room}
import my.meetings_room_renter.dao.repositories.RoomRepository.RentRepositoryService
import my.meetings_room_renter.dao.repositories.RoomRepository.RentRepositoryService.{
  getRoomFromFutureRentsInInterval,
  insertRent
}
import my.meetings_room_renter.utils.MessagesGenerator.makeSuccessfulRentNotification
import zio._

import java.sql.SQLException
import javax.sql.DataSource

object RentRoom {
  trait RentRoomService {
    def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Boolean]
    def listAllRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]]
    def rentRoom(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Either[String, String]]
    def listFutureRents(): ZIO[DataSource with RentRepositoryService, SQLException, List[Rent]]
  }

  class RentRoomServiceImpl extends RentRoomService {
    override def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Boolean] =
      RentRepositoryService.get(room).flatMap { opt =>
        if (opt.isEmpty) RentRepositoryService.insert(room).zipRight(ZIO.succeed(true)) // todo refactor it
        else ZIO.succeed(false)
      }

    override def listAllRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]] =
      RentRepositoryService.listRooms()

    override def listFutureRents(): ZIO[DataSource with RentRepositoryService, SQLException, List[Rent]] =
      RentRepositoryService.listFutureRents()

    def rentRoom(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Either[String, String]] =
      getRoomFromFutureRentsInInterval(rent)
        .flatMap(opt =>
          if (opt.isEmpty) {
            insertRent(rent)
              .fold(
                e => Left(s"${sqlStateToTextMapping.getOrElse(e.getSQLState, "Unknown error")}"),
                _ => Right(makeSuccessfulRentNotification(rent))
              )
          } else {
            ZIO.succeed(Right("Rent failed. This time was already booked."))
          }
        )
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
  }

  val live: ULayer[RentRoomServiceImpl] = ZLayer.succeed(new RentRoomServiceImpl)

}

package my.meetings_room_renter
package services

import my.meetings_room_renter.dao.entities.{Rent, Room}
import my.meetings_room_renter.dao.repositories.RoomRepository.RentRepositoryService
import my.meetings_room_renter.dao.repositories.RoomRepository.RentRepositoryService.{
  getRoomFromFutureRentsInInterval,
  insertRent
}
import zio._

import java.sql.SQLException
import javax.sql.DataSource

object RentRoom {
  trait RentRoomService {
    def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Boolean]
    def listAllRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]]
    def rentRoom(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Option[Unit]]
  }

  class RentRoomServiceImpl extends RentRoomService {
    override def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Boolean] =
      RentRepositoryService.get(room).flatMap { opt =>
        if (opt.isEmpty) RentRepositoryService.insert(room).zipRight(ZIO.succeed(true)) // todo refactor it
        else ZIO.succeed(false)
      }

    override def listAllRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]] =
      RentRepositoryService.list()

    override def rentRoom(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Option[Unit]] =
      getRoomFromFutureRentsInInterval(rent)
        .flatMap(opt =>
          if (opt.isEmpty) insertRent(rent).map(Some(_))
          else ZIO.succeed(None)
        )
  }

  object RentRoomService {
    def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Boolean] =
      ZIO.serviceWithZIO[RentRoomService](_.addNewRoom(room))

    def listAllRooms: ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, List[Room]] =
      ZIO.serviceWithZIO[RentRoomService](_.listAllRooms())

    def rentRoom(
      rent: Rent
    ): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Option[Unit]] =
      ZIO.serviceWithZIO[RentRoomService](_.rentRoom(rent))
  }

  val live: ULayer[RentRoomServiceImpl] = ZLayer.succeed(new RentRoomServiceImpl)

}

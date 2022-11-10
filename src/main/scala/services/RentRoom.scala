package my.meetings_room_renter
package services

import my.meetings_room_renter.dao.entities.Room
import my.meetings_room_renter.dao.repositories.RoomRepository.RentRepositoryService
import zio._

import java.sql.SQLException
import javax.sql.DataSource

object RentRoom {
  trait RentRoomService {
    def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Boolean]
    def listAllRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]]
  }

  class RentRoomServiceImpl extends RentRoomService {
    def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Boolean] =
      RentRepositoryService.get(room).flatMap { opt =>
        if (opt.isEmpty) RentRepositoryService.insert(room).zipRight(ZIO.succeed(true)) // todo refactor it
        else ZIO.succeed(false)
      }

    def listAllRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]] =
      RentRepositoryService.list()
  }

  object RentRoomService {
    def addNewRoom(room: Room): ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, Boolean] =
      ZIO.serviceWithZIO[RentRoomService](_.addNewRoom(room))

    def listAllRooms: ZIO[DataSource with RentRepositoryService with RentRoomService, SQLException, List[Room]] =
      ZIO.serviceWithZIO[RentRoomService](_.listAllRooms())
  }

  val live: ULayer[RentRoomServiceImpl] = ZLayer.succeed(new RentRoomServiceImpl)

}

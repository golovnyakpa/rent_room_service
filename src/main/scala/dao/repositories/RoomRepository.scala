package my.meetings_room_renter
package dao.repositories

import io.getquill.context.ZioJdbc._
import my.meetings_room_renter.dao.entities.Room
import my.meetings_room_renter.db.Ctx._
import zio._

import java.sql.SQLException
import javax.sql.DataSource

object RoomRepository {

  trait RentRepositoryService {
    def insert(room: Room): QIO[Unit]
    def getRoom(room: Room): QIO[Option[Room]]
    def list: QIO[List[Room]]
  }

  class DBServiceImpl() extends RentRepositoryService {
    val rentSchema = quote {
      querySchema[Room](""""rooms"""")
    }

    override def insert(room: Room): QIO[Unit] = run(
      rentSchema.insertValue(lift(room))
    ).unit

    override def getRoom(room: Room): QIO[Option[Room]] = run(
      rentSchema.filter(_.roomNumber == lift(room.roomNumber)).take(1)
    ).map(_.headOption)

    override def list: QIO[List[Room]] = run(rentSchema)
  }

  object RentRepositoryService {
    def insert(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Unit] =
      ZIO.serviceWithZIO[RentRepositoryService](_.insert(room))

    def get(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Option[Room]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.getRoom(room))

    def list(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.list)
  }

  val live: ULayer[RentRepositoryService] = ZLayer.succeed(new DBServiceImpl)
}

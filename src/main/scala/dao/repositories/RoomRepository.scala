package my.meetings_room_renter
package dao.repositories

import dao.entities.Room
import db.Ctx._

import io.getquill.context.ZioJdbc._
import zio._

import java.sql.SQLException
import javax.sql.DataSource


object RoomRepository {

  trait RentRepositoryService {
    def insert(room: Room): QIO[Unit]
    def get(room: Room): QIO[Option[Room]]
  }

  class DBServiceImpl() extends RentRepositoryService {
    val rentSchema = quote {
      querySchema[Room](""""rooms"""")
    }

    override def insert(room: Room): QIO[Unit] = run(
      rentSchema.insertValue(lift(room))
    ).unit

    override def get(room: Room): QIO[Option[Room]] = run(
      rentSchema.filter(_.roomNumber == lift(room.roomNumber)).take(1)
    ).map(_.headOption)
  }

  object RentRepositoryService {
    def insert(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Unit] =
      ZIO.serviceWithZIO[RentRepositoryService](_.insert(room))

    def get(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Option[Room]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.get(room))
  }

  val live: ULayer[RentRepositoryService] = ZLayer.succeed(new DBServiceImpl)
}

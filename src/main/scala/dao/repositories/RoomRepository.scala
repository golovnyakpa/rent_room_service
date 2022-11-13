package my.meetings_room_renter
package dao.repositories

import io.getquill.Query
import io.getquill.context.ZioJdbc._
import my.meetings_room_renter.dao.entities.{Rent, Room}
import my.meetings_room_renter.db.Ctx._
import zio._

import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource

case class room(room: String)

object RoomRepository {

  trait RentRepositoryService {
    def insertNewRoom(room: Room): QIO[Unit]
    def getRoom(room: Room): QIO[Option[Room]]
    def listRooms: QIO[List[Room]]
    def insertRent(rent: Rent): QIO[Long]
    def getRoomFromFutureRentsInInterval(rent: Rent): ZIO[DataSource, SQLException, Option[room]]
  }

  class DBServiceImpl() extends RentRepositoryService {
    val roomsSchema = quote {
      querySchema[Room](""""rooms"""")
    }

    val futureRentsSchema = quote {
      querySchema[Rent](""""future_rents"""")
    }

    override def insertNewRoom(room: Room): QIO[Unit] = run(
      roomsSchema.insertValue(lift(room))
    ).unit

    override def getRoom(room: Room): QIO[Option[Room]] = run(
      roomsSchema.filter(_.roomNumber == lift(room.roomNumber)).take(1)
    ).map(_.headOption)

    override def listRooms: QIO[List[Room]] = run(roomsSchema)

    //Quill doesn't seem to support range queries for dates out of the box (.isBefore/isAfter breaks it)
    //So we define an infix notation to get around this limitation
//    private implicit class RichDateTime(a: LocalDateTime) {
//      def >(b: LocalDateTime) = quote(sql"""$a > $b""".as[Boolean])
//
//      def <=(b: LocalDateTime) = quote(sql"""$a <= $b""".as[Boolean])
//    }

    override def getRoomFromFutureRentsInInterval(rent: Rent): ZIO[DataSource, SQLException, Option[room]] = {
      run(
        quote {
          sql"""
                  SELECT *
                  FROM future_rents
                  WHERE (dttm_start BETWEEN ${lift(rent.dttmStart)} AND ${lift(rent.dttmEnd)})
                               OR (dttm_end BETWEEN ${lift(rent.dttmStart)} AND ${lift(rent.dttmEnd)})
                   """.as[Query[room]]
        }
      ).map(_.headOption)
    }

    //        futureRentsSchema
//          .filter(_.room == lift(rent.room))
//          .filter(r =>
//            (r.dttmStart > lift(rent.dttmStart) && r.dttmStart <= lift(rent.dttmEnd)) ||
//              (r.dttmEnd > lift(rent.dttmStart) && r.dttmEnd <= lift(rent.dttmEnd))
//          )
//      ).map(_.headOption)
//    }

    override def insertRent(rent: Rent): QIO[Long] = run(futureRentsSchema.insertValue(lift(rent)))

  }

  object RentRepositoryService {
    def insert(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Unit] =
      ZIO.serviceWithZIO[RentRepositoryService](_.insertNewRoom(room))

    def get(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Option[Room]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.getRoom(room))

    def list(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.listRooms)

    def insertRent(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Long] =
      ZIO.serviceWithZIO[RentRepositoryService](_.insertRent(rent))

    def getRoomFromFutureRentsInInterval(
      rent: Rent
    ): ZIO[DataSource with RentRepositoryService, SQLException, Option[room]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.getRoomFromFutureRentsInInterval(rent))
  }

  val live: ULayer[RentRepositoryService] = ZLayer.succeed(new DBServiceImpl)
}

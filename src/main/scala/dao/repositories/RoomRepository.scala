package my.meetings_room_renter
package dao.repositories

import io.getquill.Quoted
import io.getquill.context.ZioJdbc._
import my.meetings_room_renter.dao.entities.{Rent, Room}
import my.meetings_room_renter.db.Ctx._
import zio._

import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource

object RoomRepository {

  trait RentRepositoryService {
    def insertNewRoom(room: Room): QIO[Unit]
    def getRoom(room: Room): QIO[Option[Room]]
    def listRooms: QIO[List[Room]]
    def insertRent(rent: Rent): QIO[Unit]
    def getRoomFromFutureRentsInInterval(rent: Rent): ZIO[DataSource, SQLException, Option[String]]
    def listFutureRents: QIO[List[Rent]]
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

    private implicit class RichDateTime(a: LocalDateTime) {
      def <=(b: LocalDateTime): Quoted[Boolean] = quote(sql"""$a <= $b""".as[Boolean])
      def >=(b: LocalDateTime): Quoted[Boolean] = quote(sql"""$a >= $b""".as[Boolean])
      def <(b: LocalDateTime): Quoted[Boolean]  = quote(sql"""$a < $b""".as[Boolean])
      def >(b: LocalDateTime): Quoted[Boolean]  = quote(sql"""$a > $b""".as[Boolean])
    }

    override def getRoomFromFutureRentsInInterval(rent: Rent): ZIO[DataSource, SQLException, Option[String]] =
      run(
        futureRentsSchema
          .filter(_.room == lift(rent.room))
          .filter(r =>
            (r.dttmStart >= lift(rent.dttmStart) && r.dttmStart < lift(rent.dttmEnd)) ||
              (r.dttmEnd > lift(rent.dttmStart) && r.dttmEnd <= lift(rent.dttmEnd))
          )
          .map(_.room)
      ).map(_.headOption)

    override def insertRent(rent: Rent): QIO[Unit] = run(futureRentsSchema.insertValue(lift(rent))).unit

    override def listFutureRents: QIO[List[Rent]] = run(futureRentsSchema)
  }

  object RentRepositoryService {
    def insert(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Unit] =
      ZIO.serviceWithZIO[RentRepositoryService](_.insertNewRoom(room))

    def get(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Option[Room]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.getRoom(room))

    def listRooms(): ZIO[DataSource with RentRepositoryService, SQLException, List[Room]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.listRooms)

    def insertRent(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Unit] =
      ZIO.serviceWithZIO[RentRepositoryService](_.insertRent(rent))

    def getRoomFromFutureRentsInInterval(
      rent: Rent
    ): ZIO[DataSource with RentRepositoryService, SQLException, Option[String]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.getRoomFromFutureRentsInInterval(rent))

    def listFutureRents(): ZIO[DataSource with RentRepositoryService, SQLException, List[Rent]] =
      ZIO.serviceWithZIO[RentRepositoryService](_.listFutureRents)
  }

  val live: ULayer[RentRepositoryService] = ZLayer.succeed(new DBServiceImpl)
}

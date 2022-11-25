package my.meetings_room_renter
package dao.repositories

import io.getquill.context.ZioJdbc._
import my.meetings_room_renter.dao.entities.{Rent, Room}
import my.meetings_room_renter.db.Ctx._
import zio._

import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource

trait RentRepositoryService {
  def insertNewRoom(room: Room): QIO[Unit]
  def getRoom(room: Room): QIO[Option[Room]]
  def listRooms: QIO[List[Room]]
  def insertRent(rent: Rent): QIO[Unit]
  def getRoomFromFutureRentsInInterval(rent: Rent): ZIO[DataSource, SQLException, Option[String]]
  def listFutureRents: QIO[List[Rent]]
  def getRent(rent: Rent): QIO[Option[Rent]]
  def updateRent(oldRent: Rent, newRent: Rent): ZIO[DataSource, SQLException, Long]
  def deleteRent(rent: Rent): ZIO[DataSource, SQLException, Long]
  def listFutureRentsForUser(user: String): QIO[List[Rent]]
}

// @accessible macros does not work, that's why forced to do this dummy job :(
object RentRepositoryService {
  def insertRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Unit] =
    ZIO.serviceWithZIO[RentRepositoryService](_.insertNewRoom(room))

  def getRoom(room: Room): ZIO[DataSource with RentRepositoryService, SQLException, Option[Room]] =
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

  def listFutureRentsForUser(user: String): ZIO[DataSource with RentRepositoryService, SQLException, List[Rent]] =
    ZIO.serviceWithZIO[RentRepositoryService](_.listFutureRentsForUser(user))

  def getRent(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Option[Rent]] =
    ZIO.serviceWithZIO[RentRepositoryService](_.getRent(rent))

  def updateRent(oldRent: Rent, newRent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Long] =
    ZIO.serviceWithZIO[RentRepositoryService](_.updateRent(oldRent, newRent))

  def deleteRent(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Long] =
    ZIO.serviceWithZIO[RentRepositoryService](_.deleteRent(rent))
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
    def <=(b: LocalDateTime) = quote(sql"""$a <= $b""".as[Boolean])
    def >=(b: LocalDateTime) = quote(sql"""$a >= $b""".as[Boolean])
    def <(b: LocalDateTime)  = quote(sql"""$a < $b""".as[Boolean])
    def >(b: LocalDateTime)  = quote(sql"""$a > $b""".as[Boolean])
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

  override def listFutureRentsForUser(user: String): QIO[List[Rent]] = run(
    futureRentsSchema.filter(_.renter == lift(user))
  )

  override def getRent(rent: Rent): QIO[Option[Rent]] = run(
    futureRentsSchema
      .filter(_.room == lift(rent.room))
      .filter(_.dttmStart == lift(rent.dttmStart))
      .filter(_.dttmEnd == lift(rent.dttmEnd))
      .filter(_.renter == lift(rent.renter))
  ).map(_.headOption)

  override def updateRent(oldRent: Rent, newRent: Rent): ZIO[DataSource, SQLException, Long] =
    run(
      futureRentsSchema
        .filter(_.room == lift(oldRent.room))
        .filter(_.dttmStart == lift(oldRent.dttmStart))
        .filter(_.dttmEnd == lift(oldRent.dttmEnd))
        .filter(_.renter == lift(oldRent.renter))
        .update(r => r.dttmStart -> lift(newRent.dttmStart), r => r.dttmEnd -> lift(newRent.dttmEnd))
    )

  def deleteRent(rent: Rent): ZIO[DataSource, SQLException, Long] =
    run(
      futureRentsSchema
        .filter(_.room == lift(rent.room))
        .filter(_.dttmStart == lift(rent.dttmStart))
        .filter(_.dttmEnd == lift(rent.dttmEnd))
        .filter(_.renter == lift(rent.renter))
        .delete
    )
}

object DBServiceImpl {
  val live: ULayer[RentRepositoryService] = ZLayer.succeed(new DBServiceImpl)
}

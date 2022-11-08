package my.meetings_room_renter
package dao.repositories

import dao.entities.Rent
import db.Ctx._

import io.getquill.context.ZioJdbc._
import zio._

import java.sql.SQLException
import javax.sql.DataSource


object RentRepository {

  trait RentRepositoryService {
    def insert(rent: Rent): QIO[Unit]
  }

  class DBServiceImpl() extends RentRepositoryService {
    val rentSchema = quote {
      querySchema[Rent](""""rent"""")
    }

    override def insert(rent: Rent): QIO[Unit] = run(
      rentSchema.insertValue(lift(rent))
    ).map(_ => ())
  }

  object RentRepositoryService {
    def insert(rent: Rent): ZIO[DataSource with RentRepositoryService, SQLException, Unit] =
      ZIO.serviceWithZIO[RentRepositoryService](_.insert(rent))
  }

  val live: ULayer[RentRepositoryService] = ZLayer.succeed(new DBServiceImpl)
}

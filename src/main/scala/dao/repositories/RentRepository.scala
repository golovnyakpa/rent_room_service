package my.meetings_room_renter
package dao.repositories

import io.getquill.context.ZioJdbc._
import my.meetings_room_renter.dao.entities.Rent
import zio.{ULayer, ZLayer}

object RentRepository {

  trait Service {
    def insert(rent: Rent): QIO[Rent]
  }

  class RentRepositoryServiceImpl() extends Service {
    override def insert(rent: Rent): QIO[Rent] = ???
  }

  val live: ULayer[Service] = ZLayer.succeed(new RentRepositoryServiceImpl)

}

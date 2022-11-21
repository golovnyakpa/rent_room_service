package my.meetings_room_renter
package dao.repositories

import my.meetings_room_renter.dao.entities.UserDb
import zio._

import java.sql.SQLException
import javax.sql.DataSource

trait UserRepository {
  def getUserByLogin(login: String): ZIO[DataSource, SQLException, Option[UserDb]]
  def registerNewUser(user: UserDb): ZIO[DataSource, SQLException, Long]
  def showAllUsers: ZIO[DataSource, SQLException, List[UserDb]]
}

object UserRepository {
  def getUserByLogin(login: String): ZIO[DataSource with UserRepository, SQLException, Option[UserDb]] =
    ZIO.serviceWithZIO[UserRepository](_.getUserByLogin(login))

  def registerNewUser(user: UserDb): ZIO[DataSource with UserRepository, SQLException, Long] =
    ZIO.serviceWithZIO[UserRepository](_.registerNewUser(user))

  def showAllUsers: ZIO[DataSource with UserRepository, SQLException, List[UserDb]] =
    ZIO.serviceWithZIO[UserRepository](_.showAllUsers)
}

case class UserRepositoryLive() extends UserRepository {

  import my.meetings_room_renter.db.Ctx._

  private val usersSchema = quote {
    querySchema[UserDb]("users")
  }

  override def getUserByLogin(login: String): ZIO[DataSource, SQLException, Option[UserDb]] = run(
    usersSchema.filter(_.login == lift(login))
  ).map(_.headOption)

  def registerNewUser(user: UserDb): ZIO[DataSource, SQLException, Long] = run(
    usersSchema.insertValue(lift(user)).returningGenerated(_.id)
  )

  def showAllUsers: ZIO[DataSource, SQLException, List[UserDb]] = run(usersSchema)

}

object UserRepositoryLive {
  val layer: ULayer[UserRepositoryLive] = ZLayer.succeed[UserRepositoryLive](UserRepositoryLive())
}

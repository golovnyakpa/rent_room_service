package my.meetings_room_renter
package auth_microservice.db_access

import cats.effect.IO
import doobie.LogHandler
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.util.transactor.Transactor.Aux
import my.meetings_room_renter.auth_microservice.Models.User
import my.meetings_room_renter.auth_microservice.hashPassword

case class DbAccess(xa: Aux[IO, Unit]) {
  def registerUser(user: User): IO[Either[String, Int]] = {
    val passwordHash = hashPassword(user.password)

    val q: doobie.Update0 = sql"insert into users (login, password) values (${user.login}, $passwordHash)" //.update
      .updateWithLogHandler(LogHandler.jdkLogHandler)

    q.run.transact(xa).attemptSomeSqlState {
      case sqlstate.class23.UNIQUE_VIOLATION => "User with such login already exists"
      case e @ _                             => s"Sql exception code: ${e.value}"
    }
  }

  def getUserByLogin(login: String): IO[Either[String, List[User]]] =
    sql"select login, password from users where login = $login"
//      .query[User]
      .queryWithLogHandler[User](LogHandler.jdkLogHandler)
      .to[List]
      .transact(xa)
      .attemptSomeSqlState { case e @ _ =>
        s"Sql exception code: ${e.value}"
      }
}

package my.meetings_room_renter
package auth_microservice

import cats.effect._
import doobie._
import doobie.implicits._
import doobie.postgres._
import my.meetings_room_renter.auth_microservice.Models.User

package object db_access {
  val xa = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5432/postgres",
    user = "postgres",
    pass = "test"
  ) // todo load it from parsed config

  val roomsQuery: IO[List[String]] =
    sql"select dttm_start from future_rents;".query[String].to[List].transact(xa)

  def registerUser(user: User): IO[Either[String, Int]] = {
    val passwordHash = hashPassword(user.password)

    val q = sql"insert into users (login, password) values (${user.login}, $passwordHash)"
      .updateWithLogHandler(LogHandler.jdkLogHandler)

    q.run.transact(xa).attemptSomeSqlState {
      case sqlstate.class23.UNIQUE_VIOLATION => "User with such login already exists"
      case e @ _                             => s"Sql exception code: ${e.value}"
    }
  }

  def getUserByLogin(login: String): IO[Either[String, List[User]]] =
    sql"select login, password from users where login = $login"
      .queryWithLogHandler[User](LogHandler.jdkLogHandler)
      .to[List]
      .transact(xa)
      .attemptSomeSqlState { case e @ _ =>
        s"Sql exception code: ${e.value}"
      }

}

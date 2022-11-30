package my.meetings_room_renter
package auth_microservice

import cats.data.EitherT
import cats.effect._
import my.meetings_room_renter.auth_microservice.db_access.DbAccess
import my.meetings_room_renter.auth_microservice.requests_handlers._
import org.http4s._
import org.http4s.dsl.io._

object AuthApi {

  def authApi(dbAccess: DbAccess): HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case req @ POST -> Root / "user" / "register" =>
        val res: EitherT[IO, String, String] = handleRegisterRequest(req, dbAccess)
        res.value.flatMap {
          case Left(value)  => Ok(s"Error happened: $value")
          case Right(value) => Ok(value)
        }
      case req @ POST -> Root / "user" / "login" =>
        handleLoginRequest(req, dbAccess).value.flatMap {
          case Left(value)  => Forbidden(s"Error happened: $value")
          case Right(value) => Ok(giveJwt(value.login))
        }
    }

}

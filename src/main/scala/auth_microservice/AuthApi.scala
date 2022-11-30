package my.meetings_room_renter
package auth_microservice

import cats.data.{EitherT, Kleisli}
import cats.effect._
import my.meetings_room_renter.auth_microservice.requests_handlers._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.client.middleware.ResponseLogger
import org.http4s.server.Router

object AuthApi {

  val authApi: HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case req @ POST -> Root / "user" / "register" =>
        val res: EitherT[IO, String, String] = handleRegisterRequest(req)
        res.value.flatMap {
          case Left(value)  => Ok(s"Error happened: $value")
          case Right(value) => Ok(value)
        }
      case req @ POST -> Root / "user" / "login" =>
        handleLoginRequest(req).value.flatMap {
          case Left(value)  => Forbidden(s"Error happened: $value")
          case Right(value) => Ok(giveJwt(value.login))
        }
    }

}

package my.meetings_room_renter
package auth_microservice

import cats.effect._
import com.comcast.ip4s._
import my.meetings_room_renter.auth_microservice.AuthApi.authApi
import org.http4s.ember.server._

object AuthApp extends IOApp {

  val server = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"9091")
    .withHttpApp(authApi)
    .build
    .use(_ => IO.never)
    .as(ExitCode.Success)

  override def run(args: List[String]): IO[ExitCode] =
    IO.println("Server started").productR(server)

}

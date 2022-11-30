package my.meetings_room_renter
package auth_microservice

import cats.effect._
import com.comcast.ip4s._
import doobie.Transactor
import my.meetings_room_renter.auth_microservice.AuthApi.authApi
import my.meetings_room_renter.auth_microservice.configuration.{Configuration, DbConf, HttpConf}
import my.meetings_room_renter.auth_microservice.db_access.DbAccess
import org.http4s.ember.server._

object AuthApp extends IOApp {

  private def createDbAccess(dbConf: DbConf): IO[DbAccess] =
    for {
      transactor <- IO.delay(
                      Transactor.fromDriverManager[IO](
                        driver = dbConf.driver,
                        url = dbConf.url,
                        user = dbConf.user,
                        pass = dbConf.pass
                      )
                    )
      dbAccess <- IO.pure(DbAccess(transactor))
    } yield dbAccess

  private def buildServer(httpConf: HttpConf, dbAccess: DbAccess): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString(httpConf.host).get)
      .withPort(Port.fromInt(httpConf.port).get)
      .withHttpApp(logRequestMiddleware(authApi(dbAccess)).orNotFound)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)

  override def run(args: List[String]): IO[ExitCode] =
    for {
      dbConf   <- IO.delay(Configuration.dbConf)
      httpConf <- IO.delay(Configuration.httpConf)
      dbAccess <- createDbAccess(dbConf)
      _        <- IO.println(s"Auth server started at http://${httpConf.host}:${httpConf.port}")
      server   <- buildServer(httpConf, dbAccess)
    } yield server
}

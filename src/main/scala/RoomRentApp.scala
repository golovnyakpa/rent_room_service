package my.meetings_room_renter

import my.meetings_room_renter.api.AppApi.appApi
import my.meetings_room_renter.configuration.HttpServerConfig
import my.meetings_room_renter.dao.repositories.{DBServiceImpl, UserRepositoryLive}
import my.meetings_room_renter.services.RentRoomServiceImpl
import zhttp.service.Server
import zio._

object RoomRentApp extends ZIOAppDefault {

//  val appEnvironment = RoomRepository.live ++ db.zioDS ++ RentRoom.live

  lazy val server = (for {
    conf <- ZIO.service[HttpServerConfig]
    _    <- Console.printLine(s"Server starting at http://${conf.host}:${conf.port}")
    _    <- Server.start(conf.port, appApi)
  } yield ExitCode.success)
    .provide(DBServiceImpl.live, db.zioDS, RentRoomServiceImpl.live, UserRepositoryLive.layer, HttpServerConfig.layer)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    server

}

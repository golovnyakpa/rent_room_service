package my.meetings_room_renter

import my.meetings_room_renter.api.AppApi.appApi
import my.meetings_room_renter.dao.repositories.DBServiceImpl
import my.meetings_room_renter.dao.repositories.UserRepositoryLive
import my.meetings_room_renter.services.RentRoomServiceImpl
import zhttp.service.Server
import zio._

object RoomRentApp extends ZIOAppDefault {

//  val appEnvironment = RoomRepository.live ++ db.zioDS ++ RentRoom.live

  lazy val server = (for {
    authedUsers <- Ref.make(List.empty[String])
    _           <- Console.printLine(s"Server starting at http://localhost:${configuration.configuration.port}")
    _           <- Server.start(configuration.configuration.port, appApi(authedUsers))
  } yield ExitCode.success).provide(DBServiceImpl.live, db.zioDS, RentRoomServiceImpl.live, UserRepositoryLive.layer)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    server

}

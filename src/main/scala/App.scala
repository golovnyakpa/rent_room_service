package my.meetings_room_renter

import my.meetings_room_renter.api.AuthenticationApi.{loginEndpoint, registerEndpoint}
import my.meetings_room_renter.api.RentRoomApi.roomApi
import my.meetings_room_renter.dao.repositories.{RoomRepository, UserRepositoryLive}
import my.meetings_room_renter.services.RentRoom
import zhttp.service.Server
import zio._

object App {

//  val appEnvironment = RoomRepository.live ++ db.zioDS ++ RentRoom.live

  lazy val server = (for {
    authedUsers <- Ref.make(List.empty[String])
    _           <- Console.printLine(s"Server starting at http://localhost:${configuration.configuration.port}")
    _           <- Server.start(configuration.configuration.port, roomApi(authedUsers))
  } yield ExitCode.success).provide(RoomRepository.live, db.zioDS, RentRoom.live, UserRepositoryLive.layer)

}

package my.meetings_room_renter

import api.RentRoomApi.rentRoomApi
import my.meetings_room_renter.dao.repositories.RoomRepository
import my.meetings_room_renter.services.RentRoom
import zhttp.service.Server
import zio._

import javax.sql.DataSource

object App {

  val appEnvironment = RoomRepository.live ++ db.zioDS ++ RentRoom.live

  lazy val server = (for {
    authedUsers <- Ref.make(List.empty[String])
    _          <- Console.printLine(s"Server starting at http://localhost:${configuration.configuration.port}")
    _          <- Server.start(configuration.configuration.port, rentRoomApi(authedUsers))
  } yield ExitCode.success).provide(appEnvironment)


}

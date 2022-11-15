package my.meetings_room_renter

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import my.meetings_room_renter.TestContainer.{cleanSchema, dockerContainerLayer, testDataSourceLayer}
import my.meetings_room_renter.dao.entities.Room
import my.meetings_room_renter.dao.repositories.RoomRepository
import my.meetings_room_renter.dao.repositories.RoomRepository.RentRepositoryService
import my.meetings_room_renter.services.RentRoom
import my.meetings_room_renter.services.RentRoom.RentRoomService
import org.testcontainers.utility.MountableFile
import zio._
import zio.test._

import javax.sql.DataSource

object UserServiceSpec extends ZIOSpecDefault {

  val layer = dockerContainerLayer >+> testDataSourceLayer ++ RoomRepository.live ++ RentRoom.live

  override def spec =
    suite("RentRoomService spec")(
      test("Method allows to insert new room") {
        for {
          rentRoomService <- ZIO.environment[RentRoomService].map(_.get)
          res <- rentRoomService
                   .addNewRoom(Room("5.01.100"))
                   .onError(_ => Console.printLine("failed").orDie.zipRight(ZIO.sleep(10.seconds)))
        } yield assertTrue(res)
      } @@ cleanSchema
    ).provideShared(layer)

}

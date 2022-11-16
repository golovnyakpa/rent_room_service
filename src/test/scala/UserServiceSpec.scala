package my.meetings_room_renter

import my.meetings_room_renter.TestContainer.{cleanSchema, dockerContainerLayer, testDataSourceLayer}
import my.meetings_room_renter.dao.entities.Room
import my.meetings_room_renter.dao.repositories.RoomRepository
import my.meetings_room_renter.services.RentRoom
import my.meetings_room_renter.services.RentRoom.RentRoomService
import zio._
import zio.test._

object UserServiceSpec extends ZIOSpecDefault {

  val layer = dockerContainerLayer >+> testDataSourceLayer ++ RoomRepository.live ++ RentRoom.live

  override def spec =
    suite("UserServiceSpec")(
      suite("Insert room")(
        test("Insert method allows to insert new room") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            res             <- rentRoomService.addNewRoom(Room("5.01.100"))
            // _ <- ZIO.sleep(100.seconds)
          } yield assertTrue(res)
        } @@ cleanSchema,
        test("Insert method dose not allow to insert room with the same number twice") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            res1            <- rentRoomService.addNewRoom(Room("5.01.101"))
            res2            <- rentRoomService.addNewRoom(Room("5.01.101"))
            _               <- Console.printLine(res1, res2)
          } yield assertTrue(res1 ^ res2)
        } @@ cleanSchema
      ), // todo разобраться, почему не работает во второй раз @@ TestAspect.nonFlaky @@ TestAspect.repeats(5)

      suite("Show inserted rooms")(
        test("Insert one room works correct") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _               <- rentRoomService.addNewRoom(Room("5.01.42"))
            res             <- rentRoomService.listAllRooms()
          } yield assertTrue(res.size == 1 && res.headOption.contains(Room("5.01.42")))
        } @@ cleanSchema,
        test("Insert few room works correct") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _               <- rentRoomService.addNewRoom(Room("5.01.42"))
            _               <- rentRoomService.addNewRoom(Room("5.01.43"))
            _               <- rentRoomService.addNewRoom(Room("5.01.42"))
            _               <- rentRoomService.addNewRoom(Room("5.01.40"))
            res             <- rentRoomService.listAllRooms()
          } yield assertTrue(res.size == 3)
        } @@ cleanSchema
      )
    ).provideShared(layer) @@ TestAspect.sequential

}

package my.meetings_room_renter

import my.meetings_room_renter.TestContainer.{cleanSchema, dockerContainerLayer, testDataSourceLayer}
import my.meetings_room_renter.dao.entities.{Rent, Room}
import my.meetings_room_renter.dao.repositories.RoomRepository
import my.meetings_room_renter.services.RentRoom
import my.meetings_room_renter.services.RentRoom.RentRoomService
import zio._
import zio.test._

object RentRoomServiceSpec extends ZIOSpecDefault {

  val layer = dockerContainerLayer >+> testDataSourceLayer ++ RoomRepository.live ++ RentRoom.live

  private def testRentInsertion(testRents: Seq[Rent]) =
    for {
      rentRoomService <- ZIO.service[RentRoomService]
      _               <- rentRoomService.addNewRoom(TestData.rooms.head)
      res             <- ZIO.foreach(testRents)(r => rentRoomService.rentRoom(r))
    } yield res

  override def spec =
    suite("UserServiceSpec")(
      suite("Insert room function")(
        test("Insert method allows to insert new room") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            res             <- rentRoomService.addNewRoom(Room("5.01.100"))
          } yield assertTrue(res)
        } @@ cleanSchema,
        test("Insert method dose not allow to insert room with the same number twice") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            res1            <- rentRoomService.addNewRoom(Room("5.01.101"))
            res2            <- rentRoomService.addNewRoom(Room("5.01.101"))
          } yield assertTrue(res1 ^ res2)
        } @@ cleanSchema
      ),
      suite("Get inserted rooms function")(
        test("Insertion of one room works correct") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _               <- rentRoomService.addNewRoom(Room("5.01.42"))
            res             <- rentRoomService.listAllRooms()
          } yield assertTrue(res.size == 1 && res.headOption.contains(Room("5.01.42")))
        } @@ cleanSchema,
        test("Insertion of few rooms works correct") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _               <- rentRoomService.addNewRoom(Room("5.01.42"))
            _               <- rentRoomService.addNewRoom(Room("5.01.43"))
            _               <- rentRoomService.addNewRoom(Room("5.01.42"))
            _               <- rentRoomService.addNewRoom(Room("5.01.40"))
            res             <- rentRoomService.listAllRooms()
          } yield assertTrue(res.size == 3)
        } @@ cleanSchema
      ),
      suite("Rent room function")(
        test("Nonoverlapping rents insertion works correct") {
          for {
            res <- testRentInsertion(TestData.nonoverlappingRents)
          } yield assertTrue(res.count(eth => eth.isRight) == TestData.nonoverlappingRents.size)
        } @@ cleanSchema,
        test("Overlapping rents are not allowed") {
          for {
            res <- testRentInsertion(TestData.overlappingRents)
          } yield assertTrue(
            res.count(eth => eth.isRight) == (TestData.overlappingRents.size / 2)
          ) // todo don't like hard bind with test data
        } @@ cleanSchema
      ),
      suite("Update rent function")(
        test("It's possible to update rent for free slot") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _               <- rentRoomService.addNewRoom(TestData.rooms.head)
            _               <- rentRoomService.rentRoom(TestData.updatedRent.oldRent)
            _               <- rentRoomService.updateRent(TestData.updatedRent)
            res             <- rentRoomService.listFutureRents()
          } yield assertTrue(
            res.size == 1 && res.headOption.contains(
              Rent(
                TestData.updatedRent.oldRent.room,
                TestData.updatedRent.dttmStart,
                TestData.updatedRent.dttmEnd,
                TestData.updatedRent.renter
              )
            )
          )
        } @@ cleanSchema,
        test("It's impossible to update rent for already booked slot") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _               <- rentRoomService.addNewRoom(TestData.rooms.head)
            _               <- rentRoomService.rentRoom(TestData.updatedRent.oldRent)
            _               <- rentRoomService.rentRoom(TestData.nonoverlappingRents(2))
            res             <- rentRoomService.updateRent(TestData.updatedRent)
            data            <- rentRoomService.listFutureRents()
          } yield assertTrue(
            res == Left("This time is not available for this room") &&
              data.contains(TestData.nonoverlappingRents(2)) &&
              data.contains(TestData.updatedRent.oldRent)
          )
        } @@ cleanSchema @@ TestAspect.nonFlaky @@ TestAspect.repeats(2)
      ),
      suite("Delete function")(
        test("Delete existing rent") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _               <- rentRoomService.addNewRoom(TestData.rooms.head)
            _               <- rentRoomService.rentRoom(TestData.nonoverlappingRents.head)
            deletedLinesNum <- rentRoomService.deleteRent(TestData.nonoverlappingRents.head)
            res             <- rentRoomService.listFutureRents()
          } yield assertTrue(res.isEmpty && deletedLinesNum == 1L)
        } @@ cleanSchema,
        test("Don't delete invalid rent") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _ <- rentRoomService.addNewRoom(TestData.rooms.head)
            _ <- rentRoomService.rentRoom(TestData.nonoverlappingRents(1))
            deletedLinesNum <- rentRoomService.deleteRent(TestData.nonoverlappingRents.head)
            res <- rentRoomService.listFutureRents()
          } yield assertTrue(res.nonEmpty && deletedLinesNum == 0L)
        } @@ cleanSchema
      )
    ).provideShared(layer) @@ TestAspect.sequential

}

package my.meetings_room_renter

import my.meetings_room_renter.TestContainer.{cleanSchema, dockerContainerLayer, testDataSourceLayer}
import my.meetings_room_renter.dao.entities.{Rent, Room, UpdatedRent, UserDb}
import my.meetings_room_renter.dao.repositories.{DBServiceImpl, UserRepository, UserRepositoryLive}
import my.meetings_room_renter.services.{RentRoomService, RentRoomServiceImpl}
import zio._
import zio.test._

object RentRoomServiceSpec extends ZIOSpecDefault {

  val layer = dockerContainerLayer >+> testDataSourceLayer ++ DBServiceImpl.live ++ RentRoomServiceImpl.live ++ UserRepositoryLive.layer

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
            res == Left("Rent failed. This time was already booked.") &&
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
            _               <- rentRoomService.addNewRoom(TestData.rooms.head)
            _               <- rentRoomService.rentRoom(TestData.nonoverlappingRents(1))
            deletedLinesNum <- rentRoomService.deleteRent(TestData.nonoverlappingRents.head)
            res             <- rentRoomService.listFutureRents()
          } yield assertTrue(res.nonEmpty && deletedLinesNum == 0L)
        } @@ cleanSchema
      ),
      suite("Access separation for different users")(
        test("User can see only his rents") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _               <- rentRoomService.addNewRoom(TestData.rooms.head)
            _               <- rentRoomService.rentRoom(TestData.nonoverlappingRents.head.copy(renter = Option("Vasya")))
            _               <- rentRoomService.rentRoom(TestData.nonoverlappingRents.tail.head.copy(renter = Option("Petya")))
            _               <- rentRoomService.rentRoom(TestData.nonoverlappingRents(2).copy(renter = Option("Vasya")))
            res1            <- rentRoomService.listFutureRentsForUser("Vasya")
            res2            <- rentRoomService.listFutureRentsForUser("Petya")
            res3            <- rentRoomService.listFutureRentsForUser("unknown")
          } yield assertTrue(res1.size == 2 &&  res2.size == 1 && res3.isEmpty)
        } @@ cleanSchema,
        test("User can delete only his rents") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _ <- rentRoomService.addNewRoom(TestData.rooms.head)
            rent1 = TestData.nonoverlappingRents.head.copy(renter = Option("Vasya"))
            rent2 = TestData.nonoverlappingRents.tail.head.copy(renter = Option("Petya"))
            _ <- rentRoomService.rentRoom(rent1)
            _ <- rentRoomService.rentRoom(rent2)
            res <- rentRoomService.deleteRent(rent1.copy(renter = Option("Petya")))
          } yield assertTrue(res == 0L)
        } @@ cleanSchema,
        test("User can update only his rents") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _ <- rentRoomService.addNewRoom(TestData.rooms.head)
            rent1 = TestData.nonoverlappingRents.head.copy(renter = Option("Vasya"))
            rent2 = TestData.nonoverlappingRents.tail.head.copy(renter = Option("Petya"))
            _ <- rentRoomService.rentRoom(rent1)
            _ <- rentRoomService.rentRoom(rent2)
            res <- rentRoomService.updateRent(UpdatedRent(rent1, rent1.dttmStart.plusHours(3L), rent1.dttmEnd.plusHours(3L), rent2.renter))
          } yield assertTrue(res.isLeft && res.left.map(_ == "Users can change only theirs rents").left.getOrElse(false))
        } @@ cleanSchema,
        test("User can't rent room for overlapping time with another renter") {
          for {
            rentRoomService <- ZIO.service[RentRoomService]
            _ <- rentRoomService.addNewRoom(TestData.rooms.head)
            rent1 = TestData.overlappingRents.head.copy(renter = Option("Vasya"))
            rent2 = TestData.overlappingRents.tail.head.copy(renter = Option("Petya"))
            _ <- rentRoomService.rentRoom(rent1)
            res <- rentRoomService.rentRoom(rent2)
          } yield assertTrue(res.isLeft && res.left.map(_ == "Rent failed. This time was already booked.").left.getOrElse(false))
        }
      )
    ).provideShared(layer) @@ TestAspect.sequential

}

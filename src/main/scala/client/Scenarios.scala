package my.meetings_room_renter
package client

import my.meetings_room_renter.dao.entities.{Rent, UpdatedRent}
import zhttp.http.Status
import zio._

object Scenarios {

  def registerNewRoom(rentRoomRequests: RentRoomRequests, roomNum: String) =
    for {
      rooms <- rentRoomRequests.showAllRooms.flatMap(r => checkStatus(r, Status.Ok, "Getting rooms") *> r.body.asString)
      _     <- ZIO.logInfo(s"Rooms: $rooms")

      _     <- rentRoomRequests.registerNewRoom(roomNum).flatMap(r => checkStatus(r, Status.Created, "Register new room"))
      rooms <- rentRoomRequests.showAllRooms.flatMap(_.body.asString)
      _     <- ZIO.logInfo(s"Rooms: $rooms")

      _     <- rentRoomRequests.registerNewRoom(roomNum).flatMap(r => checkStatus(r, Status.Conflict, "Register new room"))
      rooms <- rentRoomRequests.showAllRooms.flatMap(_.body.asString)
      _     <- ZIO.logInfo(s"Rooms: $rooms")
    } yield ()

  def addNewRent(rentRoomRequests: RentRoomRequests, rent1: Rent, rent2: Rent, isOverlappingRent: Boolean) =
    for {
      r1              <- rentRoomRequests.addRent(rent1)
      r2              <- rentRoomRequests.addRent(rent2)
      b1              <- r1.body.asString
      b2              <- r2.body.asString
      _               <- ZIO.logInfo(s"Rents bodies: $b1, $b2")
      r2ExpectedStatus = if (isOverlappingRent) Status.Conflict else Status.Created
      _               <- checkStatus(r1, Status.Created, "Rent1")
      _               <- checkStatus(r2, r2ExpectedStatus, "Rent2")
    } yield ()

  def updateExistingRent(
    rentRoomRequests: RentRoomRequests,
    rent1: Rent,
    rent2: Rent,
    updatedRent: UpdatedRent
  ) = for {
    _ <- rentRoomRequests.addRent(rent1)
    _ <- rentRoomRequests.addRent(rent2)
    r <- rentRoomRequests.updateRent(updatedRent)
    b <- r.body.asString
    _ <- ZIO.logInfo(b)
  } yield ()

  def deleteExistingRent(rentRoomRequests: RentRoomRequests, rent: Rent) =
    for {
      b <- rentRoomRequests.addRent(rent).flatMap(_.body.asString)
      _ <- ZIO.logInfo(b)
      r <- rentRoomRequests.deleteRent(rent).flatMap(_.body.asString)
      _ <- ZIO.logInfo(r)
    } yield ()

  def deleteSomeoneElseRent(
    rentRoomRequests1: RentRoomRequests,
    rentRoomRequests2: RentRoomRequests,
    rent: Rent
  ) = for {
    b <- rentRoomRequests1.addRent(rent).flatMap(_.body.asString)
    _ <- ZIO.logInfo(b)
    r <- rentRoomRequests2.deleteRent(rent).flatMap(_.body.asString)
    _ <- ZIO.logInfo(r)
  } yield ()

  def updateSomeoneElseRent(
    rentRoomRequests1: RentRoomRequests,
    rentRoomRequests2: RentRoomRequests,
    rent: Rent
  ) = for {
    b <- rentRoomRequests1.addRent(rent).flatMap(_.body.asString)
    _ <- ZIO.logInfo(b)
    r <- rentRoomRequests2
           .updateRent(UpdatedRent(rent, rent.dttmStart.plusHours(1), rent.dttmEnd.plusHours(1)))
           .flatMap(_.body.asString)
    _ <- ZIO.logInfo(r)
  } yield ()

}

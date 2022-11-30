package my.meetings_room_renter
package client

import my.meetings_room_renter.dao.entities.{Rent, UpdatedRent}
import zhttp.http._
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object AppClient extends ZIOAppDefault {

  val env: ZLayer[Any, Nothing, ChannelFactory with EventLoopGroup] = ChannelFactory.auto ++ EventLoopGroup.auto()

  val testRoomNum = "1.0.226"
  val testUser    = Option("test_user")
  val formatter   = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm")
  val baseDate    = "20220218"
  val rent1Date =
    (LocalDateTime.parse(s"$baseDate 01:00", formatter), LocalDateTime.parse(s"$baseDate 01:30", formatter))
  val rent2Date =
    (LocalDateTime.parse(s"$baseDate 16:00", formatter), LocalDateTime.parse(s"$baseDate 17:00", formatter))
  val rent3Date =
    (LocalDateTime.parse(s"$baseDate 01:15", formatter), LocalDateTime.parse(s"$baseDate 02:00", formatter))
  val rent1                  = Rent(testRoomNum, rent1Date._1, rent1Date._2, testUser)
  val rent2                  = Rent(testRoomNum, rent2Date._1, rent2Date._2)
  val rent3                  = Rent(testRoomNum, rent3Date._1, rent3Date._2)
  val updatedRent            = UpdatedRent(rent1, rent1Date._1.plusYears(1), rent1Date._2.plusYears(1))
  val updatedRentOverlapping = UpdatedRent(rent1, rent2Date._1, rent2Date._2)

  private def registerAndLogin(login: String, password: String) =
    for {
      authRequests     <- ZIO.succeed(AuthRequests(login, password))
      _                <- authRequests.register.flatMap(r => checkStatus(r, Status.Ok, "Register"))
      jwtResp          <- authRequests.getJwtToken.flatMap(r => checkStatus(r, Status.Ok, "Getting jwt"))
      jwt              <- jwtResp.body.asString
      _                <- ZIO.logInfo(s"Receive jwt: $jwt, code: ${jwtResp.status}")
      rentRoomRequests <- ZIO.succeed(RentRoomRequests(jwt))
    } yield rentRoomRequests

  val client: ZIO[EventLoopGroup with ChannelFactory, Throwable, Unit] =
    for {
      rentRoomReq <- registerAndLogin(testUser.get, "test_password")

      _ <- Scenarios.registerNewRoom(rentRoomReq, testRoomNum)
      _ <- Scenarios.addNewRent(rentRoomReq, rent1, rent2, false)
      _ <- Scenarios.addNewRent(rentRoomReq, rent1, rent3, true)
      _ <- Scenarios.updateExistingRent(rentRoomReq, rent1, rent2, updatedRentOverlapping)
      _ <- Scenarios.deleteExistingRent(rentRoomReq, rent1)
//
//      rentRoomReq2 <- registerAndLogin("qwerty", "test_password")
//      _ <- Scenarios.deleteSomeoneElseRent(rentRoomReq, rentRoomReq2, rent1)
//
//      _ <- Scenarios.addNewRent(rentRoomReq, rent1, rent2, false)
//      _ <- Scenarios.addNewRent(rentRoomReq2, rent1, rent2, false)

    } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    client.provideLayer(env).exitCode

}

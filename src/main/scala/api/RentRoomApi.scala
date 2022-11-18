package my.meetings_room_renter
package api

import my.meetings_room_renter.authentication.checkCredentialsBasicAuth
import my.meetings_room_renter.dao.entities.{Rent, Room, UpdatedRent, User}
import my.meetings_room_renter.serde._
import my.meetings_room_renter.services.RentRoom.RentRoomService
import my.meetings_room_renter.utils.RequestHandlers.parseRequest
import my.meetings_room_renter.utils.ResponseMakers
import zhttp.http._
import zio._

object RentRoomApi {

  def roomApi(authed: Ref[List[String]]) = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "rooms" =>
      parseRequest[Room](req).foldZIO(
        err => ResponseMakers.badRequestNotification(err),
        newRoom => ResponseMakers.addNewRoomIfNotExists(newRoom)
      )
    case Method.GET -> Path.root / "rooms" =>
      RentRoomService.listAllRooms.flatMap(lst => ZIO.succeed(Response.text(lst.mkString(", "))))
    case req @ Method.POST -> Path.root / "rents" =>
      parseRequest[Rent](req).foldZIO(
        err => ResponseMakers.badRequestNotification(err),
        newRent => ResponseMakers.addNewRentIfPossible(newRent)
      )
    case Method.GET -> Path.root / "rents" =>
      RentRoomService.listFutureRents.flatMap(lst => ZIO.succeed(Response.text(lst.mkString(", "))))
    case req @ Method.PUT -> Path.root / "rents" =>
      parseRequest[UpdatedRent](req).foldZIO(
        err => ResponseMakers.badRequestNotification(err),
        updatedRent => ResponseMakers.updateRentIfPossible(updatedRent)
      )
    case req @ Method.DELETE -> Path.root / "rents" =>
      parseRequest[Rent](req).foldZIO(
        err => ResponseMakers.badRequestNotification(err),
        rent => ResponseMakers.deleteRent(rent)
      )
  } @@ Middleware.basicAuthZIO(checkCredentialsBasicAuth)

  def userApi = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "users" / "register" =>
      parseRequest[User](req).foldZIO(
        err => ResponseMakers.badRequestNotification(err),
        user => ResponseMakers.registerNewUser(user)
      )
  }

  def rentRoomApi(authedUsers: Ref[List[String]]) =
    roomApi(authedUsers) ++ userApi
}

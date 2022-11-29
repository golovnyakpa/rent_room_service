package my.meetings_room_renter
package api

import my.meetings_room_renter.authentication.{checkJwt, extractLoginFromJwt}
import my.meetings_room_renter.dao.entities.{Rent, Room, UpdatedRent}
import my.meetings_room_renter.serde._
import my.meetings_room_renter.services.RentRoomService
import my.meetings_room_renter.utils.RequestHandlers.parseRequest
import my.meetings_room_renter.utils.ResponseMakers
import zhttp.http._
import zio._

object RentRoomApi {

  val roomApi = Http.collectZIO[Request] {
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
        newRent =>
          ResponseMakers.addNewRentIfPossible(newRent.copy(renter = Some(extractLoginFromJwt(req.headers.bearerToken))))
      )
    case req @ Method.GET -> Path.root / "rents" =>
//      RentRoomService.listFutureRents.flatMap(lst => ZIO.succeed(Response.text(lst.mkString(", "))))
      RentRoomService
        .listFutureRentsForUser(extractLoginFromJwt(req.headers.bearerToken))
        .flatMap(lst => ZIO.succeed(Response.text(lst.mkString(", "))))
    case req @ Method.PUT -> Path.root / "rents" =>
      parseRequest[UpdatedRent](req).foldZIO(
        err => ResponseMakers.badRequestNotification(err),
        updatedRent => ResponseMakers.updateRentIfPossible(updatedRent, req)
      )
    case req @ Method.DELETE -> Path.root / "rents" =>
      parseRequest[Rent](req).foldZIO(
        err => ResponseMakers.badRequestNotification(err),
        rent => ResponseMakers.deleteRent(rent.copy(renter = Some(extractLoginFromJwt(req.headers.bearerToken))))
      )
  } @@ Middleware.bearerAuth(checkJwt)

}

package my.meetings_room_renter
package client

import my.meetings_room_renter.dao.entities.{Rent, Room, UpdatedRent}
import my.meetings_room_renter.serde._
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.ZIO
import zio.json._

case class RentRoomRequests(jwt: String) {
  def showAllRooms: ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url                 = s"$loginHost/rooms"
    val authHeader: Headers = Headers.bearerAuthorizationHeader(jwt)
    Client.request(url, headers = authHeader, method = Method.GET)
  }

  def registerNewRoom(roomNum: String) = {
    val url                 = s"$loginHost/rooms"
    val body                = Body.fromString(Room(roomNum).toJson)
    val authHeader: Headers = Headers.bearerAuthorizationHeader(jwt)
    Client.request(url, headers = authHeader, method = Method.POST, content = body)
  }

  def addRent(rent: Rent) = {
    val url                 = s"$loginHost/rents"
    val body                = Body.fromString(rent.toJson)
    val authHeader: Headers = Headers.bearerAuthorizationHeader(jwt)
    Client.request(url, headers = authHeader, method = Method.POST, content = body)
  }

  def updateRent(updatedRent: UpdatedRent) = {
    val url                 = s"$loginHost/rents"
    val body                = Body.fromString(updatedRent.toJson)
    val authHeader: Headers = Headers.bearerAuthorizationHeader(jwt)
    Client.request(url, headers = authHeader, method = Method.PUT, content = body)
  }

  def deleteRent(rent: Rent) = {
    val url                 = s"$loginHost/rents"
    val body                = Body.fromString(rent.toJson)
    val authHeader: Headers = Headers.bearerAuthorizationHeader(jwt)
    Client.request(url, headers = authHeader, method = Method.DELETE, content = body)
  }
}

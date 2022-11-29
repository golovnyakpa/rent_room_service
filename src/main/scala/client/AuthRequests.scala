package my.meetings_room_renter
package client

import my.meetings_room_renter.dao.entities.User
import my.meetings_room_renter.serde._
import zhttp.http.{Body, Headers, Method, Response}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._
import zio.json._

case class AuthRequests(login: String, password: String) {
  def register: ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url  = s"$authHost/user/register"
    val body = Body.fromString(User(login, password).toJson)
    Client.request(url, method = Method.POST, content = body)
  }

  def getJwtToken: ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = {
    val url        = s"$authHost/user/login"
    val authHeader = Headers.basicAuthorizationHeader(login, password)
    Client.request(url, headers = authHeader, method = Method.POST)
  }
}

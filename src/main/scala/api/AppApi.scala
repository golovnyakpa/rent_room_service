package my.meetings_room_renter
package api


import my.meetings_room_renter.api.AuthenticationApi.{loginEndpoint, registerEndpoint}
import my.meetings_room_renter.api.RentRoomApi.roomApi
import zio.Ref

object AppApi {

  val authorizationApi = registerEndpoint ++ loginEndpoint
  def appApi(authedUsers: Ref[List[String]]) =
    registerEndpoint ++ (loginEndpoint ++ roomApi(authedUsers))
}

package my.meetings_room_renter
package api


import my.meetings_room_renter.api.AuthenticationApi.{loginEndpoint, registerEndpoint}
import my.meetings_room_renter.api.RentRoomApi.roomApi
import zio.Ref

object AppApi {

  def appApi(authedUsers: Ref[List[String]]) =
    roomApi(authedUsers) // interesting bug here: middleware translates to
  // other  Http's, i.e. middleware of login endpoint also translates to roomApi.
  // Issue on github: https://github.com/zio/zio-http/issues/1627
}

package my.meetings_room_renter
package api

import my.meetings_room_renter.api.RentRoomApi.roomApi
import zhttp.http._
import zio._
import zio.json._
import zhttp.http._
import zio._

object AppApi {

  def appApi =
    roomApi  // interesting bug here: middleware translates to
  // other  Http's, i.e. middleware of login endpoint also translates to roomApi.
  // Issue on github: https://github.com/zio/zio-http/issues/1627
}

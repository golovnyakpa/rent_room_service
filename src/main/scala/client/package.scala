package my.meetings_room_renter

import zhttp.http.{Response, Status}
import zio.ZIO

package object client {
  val authHost  = "http://localhost:9091"
  val loginHost = "http://localhost:9090"

  def checkStatus(resp: Response, expectedStatus: Status, stage: String) =
    if (resp.status != expectedStatus) ZIO.logError(s"$stage failed") *> ZIO.fail(new RuntimeException(stage))
    else ZIO.succeed(resp)
}

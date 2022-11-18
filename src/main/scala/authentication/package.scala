package my.meetings_room_renter

import zhttp.http.Middleware.basicAuth
import zhttp.http.middleware.Auth.Credentials

package object authentication {
  def checkCredentials(credentials: Credentials): Boolean = {
    if (credentials.uname == "admin" && credentials.uname == "admin") true else false // todo make it adequate
  }
}

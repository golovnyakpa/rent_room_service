package my.meetings_room_renter

import java.security.MessageDigest

package object auth_microservice {
  def hashPassword(password: String): String =
    MessageDigest.getInstance("SHA-256")
      .digest(password.getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
}

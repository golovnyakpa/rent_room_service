package my.meetings_room_renter

import pdi.jwt.JwtAlgorithm

package object configuration {

  val sqlStateToTextMapping: Map[String, String] = Map(
    "23503" -> "Rent update failed. Such room or rent doesn't exists"
  )

  val jwtSecretKey: String = "secretKey"
  val jwtSignatureAlgo     = JwtAlgorithm.HS256

}

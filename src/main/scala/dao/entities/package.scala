package my.meetings_room_renter
package dao

import java.time.LocalDateTime

package object entities {
  case class Room(roomNumber: String)
  case class Rent(
    room: String,
    dttmStart: LocalDateTime,
    dttmEnd: LocalDateTime,
    renter: Option[String]
  )
  case class UpdatedRent(
    oldRent: Rent,
    dttmStart: LocalDateTime,
    dttmEnd: LocalDateTime,
    renter: Option[String] = None
  )
  case class UserDb(login: String, password: String, id: Long = 0L)
  case class User(login: String, password: String)

  case class RoomJwt(roomNumber: String)

  case class RentJwt(
    room: String,
    dttmStart: LocalDateTime,
    dttmEnd: LocalDateTime,
    renter: String
  )

  case class UpdatedRentJwt(
    oldRent: Rent,
    dttmStart: LocalDateTime,
    dttmEnd: LocalDateTime,
    renter: String
  )
}

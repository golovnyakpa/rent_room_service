package my.meetings_room_renter
package dao

import java.time.LocalDateTime

package object entities {
  case class Room(roomNumber: String)
  case class Rent(
    room: String,
    dttmStart: LocalDateTime,
    dttmEnd: LocalDateTime,
    renter: String
  )
}

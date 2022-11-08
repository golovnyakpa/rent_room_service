package my.meetings_room_renter
package dao

import java.time.LocalDateTime

package object entities {
  case class Rent(dttmStart: Long, dttmEnd: Long, renter: String)
}

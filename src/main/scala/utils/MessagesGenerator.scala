package my.meetings_room_renter
package utils

import my.meetings_room_renter.dao.entities.Rent

import java.time.format.DateTimeFormatter

object MessagesGenerator {

  def makeSuccessfulRentNotification(rent: Rent): String = {
    val date = rent.dttmStart.toLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val timeFormatter = DateTimeFormatter.ofPattern("H:m")
    val startTime = rent.dttmStart.toLocalTime.format(timeFormatter)
    val endTime = rent.dttmEnd.toLocalTime.format(timeFormatter)
    s"Successfully rent for $date: $startTime-$endTime"
  }

}

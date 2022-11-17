package my.meetings_room_renter

import my.meetings_room_renter.dao.entities.{Rent, Room, UpdatedRent}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TestData {
  lazy val rooms = List(Room("5.01.100"), Room("2.02.42"), Room("9.022.52"))

  private lazy val formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm")
  private lazy val dates: List[(LocalDateTime, LocalDateTime)] = List(
    (LocalDateTime.parse("20220101 01:00", formatter), LocalDateTime.parse("20220101 01:30", formatter)),
    (LocalDateTime.parse("20220101 04:30", formatter), LocalDateTime.parse("20220101 05:00", formatter)),
    (LocalDateTime.parse("20220101 02:00", formatter), LocalDateTime.parse("20220101 03:00", formatter)),
    (LocalDateTime.parse("20220101 00:30", formatter), LocalDateTime.parse("20220101 01:15", formatter)),
    (LocalDateTime.parse("20220101 02:30", formatter), LocalDateTime.parse("20220101 03:00", formatter))
  )

  lazy val nonoverlappingRents: List[Rent] = List(
    Rent(rooms.head.roomNumber, dates.head._1, dates.head._2, "user"),
    Rent(rooms.head.roomNumber, dates(1)._1, dates(1)._2, "user"),
    Rent(rooms.head.roomNumber, dates(2)._1, dates(2)._2, "user")
  )

  lazy val overlappingRents: List[Rent] = List(
    Rent(rooms.head.roomNumber, dates.head._1, dates.head._2, "user"),
    Rent(rooms.head.roomNumber, dates(3)._1, dates(3)._2, "user"),
    Rent(rooms.head.roomNumber, dates(4)._1, dates(4)._2, "user"),
    Rent(rooms.head.roomNumber, dates(2)._1, dates(2)._2, "user")
  )

  lazy val updatedRent = UpdatedRent(nonoverlappingRents.head, dates(2)._1, dates(2)._2, "user")

}

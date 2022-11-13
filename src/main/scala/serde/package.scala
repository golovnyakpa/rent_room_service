package my.meetings_room_renter

import my.meetings_room_renter.dao.entities.{Rent, Room}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

package object serde {
  implicit val encoderRoom: JsonEncoder[Room] =
    DeriveJsonEncoder.gen[Room]
  implicit val decoderRoom: JsonDecoder[Room] =
    DeriveJsonDecoder.gen[Room]

  implicit val encoderRent: JsonEncoder[Rent] =
    DeriveJsonEncoder.gen[Rent]
  implicit val decoderRent: JsonDecoder[Rent] =
    DeriveJsonDecoder.gen[Rent]
}

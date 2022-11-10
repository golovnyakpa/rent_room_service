package my.meetings_room_renter

import my.meetings_room_renter.dao.entities.Room
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

package object serde {
    implicit val encoderRoom: JsonEncoder[Room] =
      DeriveJsonEncoder.gen[Room]
    implicit val decoderRoom: JsonDecoder[Room] =
      DeriveJsonDecoder.gen[Room]
}

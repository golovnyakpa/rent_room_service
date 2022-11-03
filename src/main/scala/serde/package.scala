package my.meetings_room_renter

import my.meetings_room_renter.dao.entities.Rent
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

package object serde {
    implicit val encoderRent: JsonEncoder[Rent] =
      DeriveJsonEncoder.gen[Rent]
    implicit val decoderRent: JsonDecoder[Rent] =
      DeriveJsonDecoder.gen[Rent]
}

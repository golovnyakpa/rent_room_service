package my.meetings_room_renter
package api

import dao.entities.Rent
import dao.repositories.RentRepository
import serde._

import zhttp.http._
import zio._
import zio.json._

import java.sql.SQLException
import javax.sql.DataSource

object RentRoomApi {

  private def handleRentResponse(req: Request): ZIO[Any, Serializable, Rent] =
    for {
      body <- req.body.asString
      res  <- ZIO.fromEither(body.fromJson[Rent])
    } yield res

  def rentRoomEndpoint(authed: Ref[List[String]]) = Http.collectZIO[Request] {
    case req @ Method.POST -> Path.root / "rent" =>
      handleRentResponse(req).foldZIO(
        err => ZIO.succeed(Response.text(err.toString).setStatus(Status.BadRequest)),
        res =>
          RentRepository.RentRepositoryService
            .insert(Rent(res.dttmStart, res.dttmEnd, res.renter))
            .zipRight(ZIO.succeed(Response.text(s"${res.toJson} inserted")))
      )

  }

  val insertEndpoint: Http[DataSource with RentRepository.RentRepositoryService, SQLException, Request, Response] =
    Http.collectZIO[Request] { case req @ Method.POST -> Path.root / "insert" =>
      RentRepository.RentRepositoryService
        .insert(Rent(0L, 122L, "Vasya"))
        .map(_ => Response.text("inserted"))
    }

  def rentRoomApi(authedUsers: Ref[List[String]]) =
    (rentRoomEndpoint(authedUsers) ++ insertEndpoint) @@ Middleware.debug
}

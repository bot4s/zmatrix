package com.bot4s.zmatrix

import zio._
import sttp.client3.Request
import io.circe._
import com.bot4s.zmatrix.models._
import java.util.UUID
import com.bot4s.zmatrix.client._
import com.bot4s.zmatrix.models.responses._
import io.circe.syntax._
import com.bot4s.zmatrix.models.RoomMessageType._
import com.bot4s.zmatrix.models.responses.EventResponse
import com.bot4s.zmatrix.models.{ EventType, RoomId, RoomMessageType }

trait RoomMembership { self: MatrixApiBase =>

  def join(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, JoinResponse] =
    sendWithAuth[JoinResponse](post(Seq("join", roomId.id)))

}

trait RoomMembershipAccessors { self: Matrix.type =>
  def join(roomId: RoomId) = ZIO.serviceWithZIO[Matrix](_.join(roomId))

}

trait Room { self: MatrixApiBase =>
  def sendEvent(roomId: RoomId, messageEvent: RoomMessageType) =
    ZIO.logDebug(messageEvent.asJson.toString()) *>
      sendWithAuth[EventResponse](
        putJson(
          Seq("rooms", roomId.id, "send", EventType.roomMessages.toString(), UUID.randomUUID().toString()),
          messageEvent.asJson
        )
      )

  def sendMsg(roomId: RoomId, message: String) =
    sendWithAuth[EventResponse](
      putJson(
        Seq("rooms", roomId.id, "send", EventType.roomMessages.toString(), UUID.randomUUID().toString()),
        RoomMessageTextContent(message).asJson
      )
    )
}

private[zmatrix] trait RoomAccessors {
  def sendEvent(roomId: RoomId, messsageEvent: RoomMessageType) =
    ZIO.serviceWithZIO[Matrix](_.sendEvent(roomId, messsageEvent))
  def sendMsg(roomId: RoomId, message: String) =
    ZIO.serviceWithZIO[Matrix](_.sendMsg(roomId, message))
}

trait Sync { self: MatrixApiBase =>
  def sync: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
    for {
      request <- get(Seq("sync"))
      request <- withSince(request)
      request <- authenticate(request)
      result  <- send(request)
      decoded <- as[SyncState](result)
    } yield decoded
}

private[zmatrix] trait SyncAccessors {
  def sync = ZIO.serviceWithZIO[Matrix](_.sync)
}

trait Login { self: MatrixApiBase =>
  def passwordLogin(
    user: String,
    password: String,
    deviceId: Option[String]
  ) = {
    val json = Json
      .obj(
        "type"      -> Json.fromString(EventType.passwordLogin.toString()),
        "user"      -> Json.fromString(user),
        "password"  -> Json.fromString(password),
        "device_id" -> deviceId.map(Json.fromString(_)).getOrElse(Json.Null)
      )
      .deepDropNullValues

    send[LoginResponse](postJson(Seq("login"), json))
  }

  def tokenLogin(
    token: String,
    deviceId: Option[String] = None
  ): ZIO[MatrixEnv, MatrixError, LoginResponse] = {
    val json = Json.obj(
      "type"      -> Json.fromString(EventType.tokenLogin.toString()),
      "token"     -> Json.fromString(token),
      "device_id" -> deviceId.map(Json.fromString(_)).getOrElse(Json.Null)
    )

    send[LoginResponse](postJson(Seq("login"), json))
  }
}

private[zmatrix] trait LoginAccessors { self: Matrix.type =>
  def passwordLogin(
    user: String,
    password: String,
    deviceId: Option[String]
  ) = ZIO.serviceWithZIO[Matrix](_.passwordLogin(user, password, deviceId))

  def tokenLogin(
    token: String,
    deviceId: Option[String] = None
  ) =
    ZIO.serviceWithZIO[Matrix](_.tokenLogin(token, deviceId))
}

trait MatrixApiBase extends MatrixRequests with WithAccess with MatrixParser {
  def client: MatrixClient

  def send[T](request: Request[MatrixResponse[T], Any]): IO[MatrixError, T] = client.send(request)

  def send[T](action: MatrixAction)(implicit decoder: Decoder[T]): ZIO[MatrixConfiguration, MatrixError, T] =
    for {
      req      <- action
      response <- send(req)
      decoded  <- as(response)(decoder)
    } yield decoded

  def sendWithAuth[T](
    action: MatrixAction
  )(implicit decoder: Decoder[T]): ZIO[AuthMatrixEnv, MatrixError, T] =
    for {
      req      <- action
      auth     <- authenticate(req)
      response <- send(auth)
      decoded  <- as(response)(decoder)
    } yield decoded

}

trait Account { self: MatrixApiBase =>
  def whoAmI: ZIO[AuthMatrixEnv, MatrixError, UserResponse] =
    sendWithAuth[UserResponse](get(Seq("account", "whoami")))
}

private[zmatrix] trait AccountAccessors { self: Matrix.type =>
  def whoAmI: ZIO[Matrix with AuthMatrixEnv, MatrixError, UserResponse] =
    ZIO.serviceWithZIO[Matrix](_.whoAmI)
}

trait Matrix extends MatrixApiBase with Login with Account with Sync with Room with RoomMembership

object Matrix
    extends LoginAccessors
    with AccountAccessors
    with SyncAccessors
    with RoomAccessors
    with RoomMembershipAccessors {
  def make: ZLayer[MatrixClient, Nothing, Matrix] = ZLayer {
    ZIO.serviceWith[MatrixClient](c =>
      new Matrix {
        val client = c
      }
    )
  }
}

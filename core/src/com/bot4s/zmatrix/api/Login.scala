package com.bot4s.zmatrix.api

import zio.ZIO
import zio.json.ast.Json

import com.bot4s.zmatrix.models.EventType
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.{ Matrix, MatrixApiBase, MatrixEnv, MatrixError }

trait Login { self: MatrixApiBase =>
  def passwordLogin(
    user: String,
    password: String,
    deviceId: Option[String]
  ) = {
    val json = Json
      .Obj(
        "type"      -> Json.Str(EventType.passwordLogin.toString()),
        "user"      -> Json.Str(user),
        "password"  -> Json.Str(password),
        "device_id" -> deviceId.map(Json.Str(_)).getOrElse(Json.Null)
      )

    send[LoginResponse](postJson(Seq("login"), json))
  }

  def tokenLogin(
    token: String,
    deviceId: Option[String] = None
  ): ZIO[MatrixEnv, MatrixError, LoginResponse] = {
    val json = Json.Obj(
      "type"      -> Json.Str(EventType.tokenLogin.toString()),
      "token"     -> Json.Str(token),
      "device_id" -> deviceId.map(Json.Str(_)).getOrElse(Json.Null)
    )

    send[LoginResponse](postJson(Seq("login"), json))
  }
}

private[zmatrix] trait LoginAccessors {
  def passwordLogin(user: String, password: String, deviceId: Option[String]) =
    ZIO.serviceWithZIO[Matrix](_.passwordLogin(user, password, deviceId))

  def tokenLogin(token: String, deviceId: Option[String] = None) =
    ZIO.serviceWithZIO[Matrix](_.tokenLogin(token, deviceId))
}

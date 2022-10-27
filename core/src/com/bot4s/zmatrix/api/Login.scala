package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.models.EventType
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.{ MatrixEnv, MatrixError }
import io.circe.Json

trait Login {

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

object login extends Login

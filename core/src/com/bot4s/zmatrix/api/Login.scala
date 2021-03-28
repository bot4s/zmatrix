package com.bot4s.zmatrix.api

import zio.ZIO
import io.circe.Json
import com.bot4s.zmatrix.{ MatrixEnv, MatrixError }
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.models.EventType

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

    val request = postJson(Seq("login"), json)
    (request >>= send) >>= as[LoginResponse]
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

    val request = postJson(Seq("login"), json)
    (request >>= send) >>= as[LoginResponse]
  }

}

object login extends Login

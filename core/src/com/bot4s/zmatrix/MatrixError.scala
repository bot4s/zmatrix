package com.bot4s.zmatrix

import zio.json._

sealed trait MatrixError extends Exception

object MatrixError {
  final case class NetworkError(error: String, underlying: Throwable)
      extends Exception(s"Network Error $error")
      with MatrixError

  final case class SerializationError(body: String, error: String)
      extends Exception(s"Serialization error $body $error")
      with MatrixError

  final case class InvalidParameterError(name: String, msg: String)
      extends Exception(s"Invalid parameter $name: $msg")
      with MatrixError

  final case class ResponseError(errcode: String, error: String, softLogout: Option[Boolean] = None)
      extends Exception(s"Matrix API error: ($errcode, $error)")
      with MatrixError

  final case class ClientError(error: String, underlying: Throwable)
      extends Exception(s"Client Error $error")
      with MatrixError

  implicit val responseErrorDecoder: JsonDecoder[ResponseError] =
    DeriveJsonDecoder.gen[ResponseError]

}

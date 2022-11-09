package com.bot4s.zmatrix.client

import zio.json.JsonDecoder
import zio.json.ast.Json
import zio.{ IO, ZIO }

import com.bot4s.zmatrix.MatrixError._

trait MatrixParser {
  def as[T](json: Json)(implicit decoder: JsonDecoder[T]): IO[SerializationError, T] =
    ZIO.fromEither(json.as[T]).mapError { decoding =>
      SerializationError(json.toString, decoding)
    }
}

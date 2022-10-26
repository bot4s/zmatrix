package com.bot4s.zmatrix.client

import zio.{ IO, ZIO }

import com.bot4s.zmatrix.MatrixError._
import io.circe.{ Decoder, Json }

trait MatrixParser {
  def as[T](json: Json)(implicit decoder: Decoder[T]): IO[SerializationError, T] =
    ZIO.fromEither(json.as[T]).mapError { decoding =>
      SerializationError(json.toString, decoding)
    }
}

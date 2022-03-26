package com.bot4s.zmatrix.client

import zio.{ IO, ZIO }
import io.circe.{ Decoder, Json }
import com.bot4s.zmatrix.MatrixError._

trait MatrixParser {
  def as[T](json: Json)(implicit decoder: Decoder[T]): IO[SerializationError, T] =
    ZIO.fromEither(json.as[T]).mapError { decoding =>
      SerializationError(json.toString, decoding)
    }
}

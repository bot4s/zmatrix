package com.bot4s.zmatrix.client

import zio.IO
import io.circe.{ Decoder, Json }
import com.bot4s.zmatrix.MatrixError._

trait MatrixParser {
  def as[T](json: Json)(implicit decoder: Decoder[T]): IO[SerializationError, T] =
    IO.fromEither(json.as[T]).mapError { decoding =>
      SerializationError(json.toString, decoding)
    }
}

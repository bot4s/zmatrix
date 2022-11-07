package com.bot4s.zmatrix.core

import io.circe.Json
import sttp.model.MediaType

sealed trait MatrixBody
object MatrixBody {
  case object EmptyBody                                          extends MatrixBody
  case class JsonBody(json: Json)                                extends MatrixBody
  case class ByteBody(body: Array[Byte], contentType: MediaType) extends MatrixBody

  val empty = EmptyBody
}

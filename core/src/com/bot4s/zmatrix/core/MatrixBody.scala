package com.bot4s.zmatrix.core

import zio.json._

import sttp.model.MediaType

sealed trait MatrixBody

object MatrixBody {
  case object EmptyBody extends MatrixBody
  case class JsonBody[T: JsonEncoder](json: T) extends MatrixBody {
    val encoder = implicitly[JsonEncoder[T]]
    def encode  = json.toJson
  }
  case class ByteBody(body: Array[Byte], contentType: MediaType) extends MatrixBody

  val empty = EmptyBody
}

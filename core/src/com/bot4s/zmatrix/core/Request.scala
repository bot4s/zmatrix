package com.bot4s.zmatrix.core

import io.circe.{ Decoder, DecodingFailure, Error, Json }
import sttp.model.Method
import sttp.client3.ResponseAs
import sttp.client3.ResponseException
import sttp.client3.json.RichResponseAs
import com.bot4s.zmatrix.MatrixResponse
import com.bot4s.zmatrix.MatrixError
import com.bot4s.zmatrix.MatrixError._
import sttp.client3._
import sttp.client3.{ Request => HttpRequest }
import sttp.client3.circe._
import com.bot4s.zmatrix.core.MatrixBody.EmptyBody

sealed trait MatrixBody
object MatrixBody {
  case object EmptyBody           extends MatrixBody
  case class JsonBody(json: Json) extends MatrixBody

  val empty = EmptyBody
}

final case class Request[T](
  method: Method,
  path: Seq[String],
  body: MatrixBody,
  responseAs: Json => Either[DecodingFailure, T]
) {

  def toRequest(
    baseUri: String
  ): HttpRequest[MatrixResponse[Json], Any] = {
    // val respond: ResponseAs[MatrixResponse[T], Any] =
    //   respondWith(
    //     deserializeJson(SlackResponse.decodeWith(responseAs), implicitly[IsOption[SlackResponse[T]]])
    //   ).mapWithMetadata {
    //     case (Left(DeserializationException(body, error)), _) => MatrixError.SerializationError(body, error)
    //     case (Left(HttpError(body, code)), _)                 => MatrixError.ResponseError(code.code.toString(), body)
    //     case (Right(response), _)                             => response
    //   }

    val req = body match {
      case EmptyBody =>
        basicRequest
          .method(method, uri"$baseUri/$path")
      case MatrixBody.JsonBody(json) =>
        basicRequest
          .method(method, uri"$baseUri/$path")
          .body(json.deepDropNullValues)
    }
    req.response(asJsonEither[MatrixError.ResponseError, Json])
  }
}

object Request {

  private def extractBodyAt[A: Decoder](key: String): Decoder[A] =
    Decoder.instance[A](_.downField(key).as[A])

  private def respondWith[A](
    f: String => Either[io.circe.Error, A]
  ): ResponseAs[Either[ResponseException[String, Error], A], Any] =
    asString.mapWithMetadata(ResponseAs.deserializeRightWithError(f)).showAsJson

  def make(method: Method, path: Seq[String], body: MatrixBody = MatrixBody.empty): Request[Unit] =
    Request(method, path, body, _ => Right(()))

}

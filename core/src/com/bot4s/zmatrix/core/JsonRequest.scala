package com.bot4s.zmatrix.core

import zio.json.ast.Json

import com.bot4s.zmatrix.MatrixError
import com.bot4s.zmatrix.core.MatrixBody.EmptyBody
import com.bot4s.zmatrix.core.RequestAuth._
import sttp.client3._
import sttp.client3.ziojson._
import sttp.client3.{ Request => HttpRequest }
import sttp.model.Method

/*
  As of now the request class is as simple as it could be
  we could also simple wrap a `RequestT` from sttp, but we might loose
  some customization and delayed configuration.

  This also assume that we will only have to deal with JSON. There are no
  counter-indication in matrix' documentation but in order to support
  other kind of body, we would have to deal with `ResponseAs` and other
  pretty complexe topic that I don't want to introduce unless required
 */
final case class JsonRequest(
  method: Method,
  path: Seq[String],
  body: MatrixBody = EmptyBody,
  params: Seq[(String, Option[String])] = Seq.empty,
  auth: RequestAuth = RequestAuth.NoAuth,
  scope: ApiScope = ApiScope.Client
) {

  def withScope(scope: ApiScope)                = copy(scope = scope)
  def withAuth(auth: RequestAuth)               = copy(auth = auth)
  def addParam(param: (String, Option[String])) = copy(params = params :+ param)

  def body(body: MatrixBody) = copy(body = body)

  def toRequest(
    baseUri: String
  ): HttpRequest[Either[MatrixError, Json], Any] = {
    val req = body match {
      case EmptyBody =>
        basicRequest
          .method(method, uri"$baseUri/$path")
      case body @ MatrixBody.JsonBody(json) =>
        implicit val encoder = body.encoder
        basicRequest
          .method(method, uri"$baseUri/$path")
          .body(json)
      case MatrixBody.ByteBody(body, contentType) =>
        basicRequest
          .method(method, uri"$baseUri/$path")
          .contentType(contentType)
          .body(body)
    }
    val withParams = params.foldLeft(req) { case (req, (paramName, paramValue)) =>
      val uri = req.uri.addParam(paramName, paramValue)
      req.copy[Identity, Either[String, String], Any](uri = uri)
    }
    val withAuth = auth match {
      case NoAuth           => withParams
      case TokenAuth(token) => withParams.auth.bearer(token)
    }

    withAuth
      .response(asJsonEither[MatrixError.ResponseError, Json])
      .mapResponse(_.left.map {
        case HttpError(error, _)                   => error
        case DeserializationException(body, error) => MatrixError.SerializationError(body, error)
      })
  }
}

object JsonRequest {

  def make(method: Method, path: Seq[String], body: MatrixBody = MatrixBody.empty): JsonRequest =
    JsonRequest(method, path, body)

}

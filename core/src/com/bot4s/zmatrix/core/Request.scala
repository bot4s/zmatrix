package com.bot4s.zmatrix.core

import com.bot4s.zmatrix.MatrixError
import com.bot4s.zmatrix.core.MatrixBody.EmptyBody
import com.bot4s.zmatrix.core.RequestAuth._
import io.circe.Json
import sttp.client3._
import sttp.client3.circe._
import sttp.client3.{ Request => HttpRequest }
import sttp.model.{ MediaType, Method }

sealed trait MatrixBody
object MatrixBody {
  case object EmptyBody                                          extends MatrixBody
  case class JsonBody(json: Json)                                extends MatrixBody
  case class ByteBody(body: Array[Byte], contentType: MediaType) extends MatrixBody

  val empty = EmptyBody
}
sealed trait RequestAuth
object RequestAuth {
  case object NoAuth                  extends RequestAuth
  case class TokenAuth(token: String) extends RequestAuth
}

sealed trait ApiScope

object ApiScope {
  case object Client extends ApiScope
  case object Media  extends ApiScope
}

final case class Request(
  method: Method,
  path: Seq[String],
  body: MatrixBody = EmptyBody,
  params: Seq[(String, Option[String])] = Seq.empty,
  auth: RequestAuth = RequestAuth.NoAuth,
  scope: ApiScope = ApiScope.Client
) {

  def toRequest(
    baseUri: String
  ): HttpRequest[Either[ResponseException[MatrixError.ResponseError, io.circe.Error], Json], Any] = {
    val req = body match {
      case EmptyBody =>
        basicRequest
          .method(method, uri"$baseUri/$path")
      case MatrixBody.JsonBody(json) =>
        basicRequest
          .method(method, uri"$baseUri/$path")
          .body(json.deepDropNullValues)
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
    withAuth.response(asJsonEither[MatrixError.ResponseError, Json])
  }
}

object Request {

  def make(method: Method, path: Seq[String], body: MatrixBody = MatrixBody.empty): Request =
    Request(method, path, body)

}
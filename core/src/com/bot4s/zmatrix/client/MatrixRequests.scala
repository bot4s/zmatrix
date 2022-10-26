package com.bot4s.zmatrix.client

import zio.{ URIO, ZIO }

import com.bot4s.zmatrix.MatrixError.ResponseError
import com.bot4s.zmatrix.{ MatrixConfiguration, _ }
import io.circe.{ Error, Json }
import sttp.client3.circe._
import sttp.client3.{ ResponseException, _ }
import sttp.model.{ MediaType, Method }

/**
 * This trait provides all the helper methods related to the queries that must
 * be send to the Matrix server. It must be used to serialize requests' body and
 * query parameters.
 */
trait MatrixRequests {
  type MatrixResponseError = ResponseException[ResponseError, Error]
  type MatrixResponse[T]   = Either[MatrixResponseError, T]
  type MatrixAction        = URIO[MatrixConfiguration, Request[MatrixResponse[Json], Any]]

  def get(path: Seq[String]): MatrixAction =
    requestWithPath(Method.GET, path).map(_.response(asJsonEither[ResponseError, Json]))

  def postJson(path: Seq[String], body: Json): MatrixAction =
    sendJson(Method.POST, path, body)

  def putJson(path: Seq[String], body: Json): MatrixAction =
    sendJson(Method.PUT, path, body)

  def post(path: Seq[String]): MatrixAction =
    postJson(path, Json.obj())

  /*
    This method is a bit specific as it is only for `media` file
    this might need a refactor in the future, but as of now,
    it can not be used to send another file
   */
  def uploadMediaFile(content: Array[Byte], contentType: MediaType): MatrixAction =
    ZIO.serviceWithZIO[MatrixConfiguration] { config =>
      config.get.map { config =>
        basicRequest
          .method(Method.POST, uri"${config.matrix.mediaApi}/upload")
          .body(content)
          .contentType(contentType)
          .response(asJsonEither[ResponseError, Json])
      }
    }

  def withSince(
    request: Request[MatrixResponse[Json], Any]
  ): URIO[AuthMatrixEnv, Request[MatrixResponse[Json], Any]] =
    ZIO.serviceWithZIO[SyncTokenConfiguration] { config =>
      config.get.map { config =>
        val uriWithParam = request.uri.addParam("since", config.since)
        request.copy[Identity, MatrixResponse[Json], Any](uri = uriWithParam)
      }
    }

  /**
   * Private helpers to reuse component such as config extractions
   * Query parameters and serialization
   */

  private def requestWithPath(method: Method, path: Seq[String]) =
    ZIO.serviceWithZIO[MatrixConfiguration] { config =>
      config.get.map { config =>
        basicRequest
          .method(method, uri"${config.matrix.clientApi}/$path")
      }
    }

  private def sendJson(
    method: Method,
    path: Seq[String],
    body: Json
  ) =
    requestWithPath(method, path).map(
      _.body(body.deepDropNullValues)
        .response(asJsonEither[ResponseError, Json])
    )
}

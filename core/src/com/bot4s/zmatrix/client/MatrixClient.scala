package com.bot4s.zmatrix.client

import zio.json.ast.Json
import zio.{ IO, Task, URLayer, ZIO, ZLayer }

import com.bot4s.zmatrix.MatrixError.NetworkError
import com.bot4s.zmatrix._
import com.bot4s.zmatrix.core.{ ApiScope, JsonRequest }
import sttp.client3._

/**
 * This service provide the low lever interface with Matrix.
 * It should be able to send a request to the API and decode the result into a MatrixResponse
 * The underlying errors must be wrapped into a subtype of MatrixError
 */
trait MatrixClient {
  def send(request: JsonRequest): IO[MatrixError, Json]
}

object MatrixClient {

  def send(request: JsonRequest): ZIO[MatrixClient, MatrixError, Json] =
    ZIO.environmentWithZIO(_.get.send(request))

  def live: URLayer[MatrixConfiguration with SttpBackend[Task, Any], MatrixClient] =
    ZLayer.fromFunction(LiveMatrixClient.apply _)
}

final case class LiveMatrixClient(backend: SttpBackend[Task, Any], config: MatrixConfiguration) extends MatrixClient {

  override def send(
    request: JsonRequest
  ): IO[MatrixError, Json] =
    for {
      _ <- ZIO.unit
      prefix = request.scope match {
                 case ApiScope.Client => config.matrix.clientApi
                 case ApiScope.Media  => config.matrix.mediaApi
               }
      httpRequest = request.toRequest(prefix)
      _          <- ZIO.logDebug(httpRequest.toCurl)
      result <- backend
                  .send(httpRequest.response(asBoth(httpRequest.response, asStringAlways)))
                  .mapError(error => NetworkError(f"Error contacting matrix server: ${error.toString()}", error))

      (parsed, raw) = result.body
      _            <- ZIO.logTrace(raw)

      json <- ZIO.fromEither(parsed)
    } yield json
}

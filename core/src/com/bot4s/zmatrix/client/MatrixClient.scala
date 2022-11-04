package com.bot4s.zmatrix.client

import zio.{ IO, Task, URLayer, ZIO, ZLayer }

import com.bot4s.zmatrix.MatrixError.{ NetworkError, SerializationError }
import com.bot4s.zmatrix.MatrixConfiguration
import com.bot4s.zmatrix.{ MatrixError, MatrixResponse }
import com.bot4s.zmatrix.core.Request
import sttp.client3._
import io.circe.Decoder

/**
 * This service provide the low lever interface with Matrix.
 * It should be able to send a request to the API and decode the result into a MatrixResponse
 * The underlying errors must be wrapped into a subtype of MatrixError
 */
trait MatrixClient {
  def send[T](request: Request[T]): IO[MatrixError, T]
}

object MatrixClient {

  def send[T](request: Request[T]): ZIO[MatrixClient, MatrixError, T] =
    ZIO.environmentWithZIO(_.get.send(request))

  def live: URLayer[MatrixConfiguration with SttpBackend[Task, Any], MatrixClient] =
    ZLayer.fromFunction(LiveMatrixClient.apply _)
}

final case class LiveMatrixClient(backend: SttpBackend[Task, Any], matrixConfig: MatrixConfiguration)
    extends MatrixClient {

  override def send[T](
    request: Request[T]
  ): IO[MatrixError, T] =
    for {
      config     <- matrixConfig.get
      httpRequest = request.toRequest(config.matrix.clientApi)
      _          <- ZIO.logDebug(httpRequest.toCurl)
      result <- backend
                  .send(httpRequest.response(asBoth(httpRequest.response, asStringAlways)))
                  .mapError(t => NetworkError(f"Error contacting matrix server: ${t.toString()}", t))
      _ <- ZIO.logTrace(result.body._2)
      json <- ZIO.fromEither(result.body._1).mapError {
                case httpError: HttpError[MatrixError] => httpError.body
                case deserialisationError: DeserializationException[_] =>
                  SerializationError(deserialisationError.body, deserialisationError.error)
                case x => NetworkError(f"Unknown error: ${x.toString()}", x)
              }
    } yield ???
}

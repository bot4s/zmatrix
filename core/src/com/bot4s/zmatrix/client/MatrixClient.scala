package com.bot4s.zmatrix.client

import zio.{ IO, Task, URLayer, ZIO, ZLayer }

import com.bot4s.zmatrix.MatrixError.{ NetworkError, SerializationError }
import com.bot4s.zmatrix.{ MatrixError, MatrixResponse }
import sttp.client3._

/**
 * This service provide the low lever interface with Matrix.
 * It should be able to send a request to the API and decode the result into a MatrixResponse
 * The underlying errors must be wrapped into a subtype of MatrixError
 */
trait MatrixClient {
  def send[T](request: Request[MatrixResponse[T], Any]): IO[MatrixError, T]
}

object MatrixClient {

  def send[T](request: Request[MatrixResponse[T], Any]): ZIO[MatrixClient, MatrixError, T] =
    ZIO.environmentWithZIO(_.get.send(request))

  def live: URLayer[SttpBackend[Task, Any], MatrixClient] =
    ZLayer.fromFunction(LiveMatrixClient.apply _)
}

final case class LiveMatrixClient(backend: SttpBackend[Task, Any]) extends MatrixClient {

  override def send[T](
    request: Request[MatrixResponse[T], Any]
  ): IO[MatrixError, T] =
    for {
      _ <- ZIO.logDebug(request.toCurl)
      result <- backend
                  .send(request.response(asBoth(request.response, asStringAlways)))
                  .mapError(t => NetworkError(f"Error contacting matrix server: ${t.toString()}", t))
      _ <- ZIO.logTrace(result.body._2)
      json <- ZIO.fromEither(result.body._1).mapError {
                case httpError: HttpError[MatrixError] => httpError.body
                case deserialisationError: DeserializationException[_] =>
                  SerializationError(deserialisationError.body, deserialisationError.error)
                case x => NetworkError(f"Unknown error: ${x.toString()}", x)
              }
    } yield json
}

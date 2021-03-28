package com.bot4s.zmatrix.client

import zio.logging._
import sttp.client3.asynchttpclient.zio.{ SttpClient, send => sendRequest }
import sttp.client3.{ asBoth, asStringAlways, Request }
import zio.{ Has, IO, URLayer, ZIO, ZLayer }
import com.bot4s.zmatrix.MatrixError.{ NetworkError, SerializationError }
import com.bot4s.zmatrix.{ MatrixError, MatrixResponse }
import sttp.client3.{ DeserializationException, HttpError }

/**
 * This service provide the low lever interface with Matrix.
 * It should be able to send a request to the API and decode the result into a MatrixResponse
 * The underlying errors must be wrapped into a subtype of MatrixError
 */
trait MatrixClient {
  def send[T](
    request: Request[MatrixResponse[T], Any]
  ): IO[MatrixError, T]
}

object MatrixClient {

  def send[T](request: Request[MatrixResponse[T], Any]): ZIO[Has[MatrixClient], MatrixError, T] =
    ZIO.accessM(_.get.send(request))

  def live: URLayer[SttpClient with Logging, Has[MatrixClient]] =
    ZLayer.fromFunction[SttpClient with Logging, MatrixClient] { client =>
      new MatrixClient {
        override def send[T](
          request: Request[MatrixResponse[T], Any]
        ): IO[MatrixError, T] =
          (for {
            _ <- log.debug(request.toCurl)
            result <- sendRequest(request.response(asBoth(request.response, asStringAlways)))
                        .mapError(t => NetworkError(f"Error contacting matrix server: ${t.toString()}", t))
            _ <- log.trace(result.body._2)
            json <- ZIO.fromEither(result.body._1).mapError {
                      case httpError: HttpError[MatrixError] => httpError.body
                      case deserialisationError: DeserializationException[_] =>
                        SerializationError(deserialisationError.body, deserialisationError.error)
                      case x => NetworkError(f"Unknown error: ${x.toString()}", x)
                    }
          } yield json).provide(client)
      }
    }
}

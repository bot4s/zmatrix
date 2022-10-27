package com.bot4s.zmatrix

import zio.{ Schedule, _ }

import com.bot4s.zmatrix.MatrixError.{ NetworkError, ResponseError }
import com.bot4s.zmatrix.client.MatrixClient
import com.bot4s.zmatrix.services.Authentication
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend

trait ExampleApp[T] extends zio.ZIOAppDefault {

  def runExample: ZIO[AuthMatrixEnv, MatrixError, T]

  override def run: ZIO[Environment, Any, ExitCode] =
    runExample.catchSome { case ResponseError("M_MISSING_TOKEN", _, _) | ResponseError("M_UNKNOWN_TOKEN", _, _) =>
      for {
        _ <- ZIO.logError("Invalid or empty token provided, trying password authentication")
        // We want to retry authentication only in case of network error, any other error should terminate the fiber instead
        _ <- Authentication.refresh
               .tapError(x => ZIO.logError(x.toString()))
               .refineOrDie { case x: NetworkError => x }
               .retry(Schedule.exponential(1.seconds))
               .tap(token => ZIO.logInfo(token.token))
        program <- runExample
      } yield program
    }
      .tapError(error => ZIO.logError(error.toString()))
      .retry(Schedule.forever)
      .exitCode
      .provide(
        SyncTokenConfiguration
          .persistent()
          .mapError(x => new Exception(s"Unable to read token configuration $x"))
          .orDie,
        MatrixConfiguration.live().mapError(x => new Exception(s"Unable to read configuration $x")).orDie,
        Authentication.live,
        AsyncHttpClientZioBackend.layer().orDie,
        MatrixClient.live
      )

}

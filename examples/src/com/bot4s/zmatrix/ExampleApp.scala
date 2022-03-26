package com.bot4s.zmatrix

import zio._
import zio.Console._
import com.bot4s.zmatrix.api.{ accounts, roomMembership }
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.client.MatrixClient
import com.bot4s.zmatrix.MatrixError.{ NetworkError, ResponseError }

import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import com.bot4s.zmatrix.services.Authentication
import zio.Schedule

trait ExampleApp[T] extends zio.App {

  def runExample(args: List[String]): ZIO[AuthMatrixEnv, MatrixError, T]

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val effect = runExample(args)
    effect.catchSome { case ResponseError("M_MISSING_TOKEN", _, _) | ResponseError("M_UNKNOWN_TOKEN", _, _) =>
      for {
        _ <- ZIO.logError("Invalid or empty token provided, trying password authentication")
        // We want to retry authentication only in case of network error, any other error should terminate the fiber instead
        _ <- Authentication.refresh
               .tapError(x => ZIO.logError(x.toString()))
               .refineOrDie { case x: NetworkError => x }
               .retry(Schedule.exponential(1.seconds))
               .tap(token => ZIO.logInfo(token.token))
        program <- effect
      } yield program
    }
      .tapError(error => ZIO.logError(error.toString()))
      .retry(Schedule.forever)
      .exitCode
      .provide(
        ZEnv.live,
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

}

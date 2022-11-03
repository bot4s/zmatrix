package com.bot4s.zmatrix

import zio.{ Schedule, _ }

import com.bot4s.zmatrix.client.MatrixClient
import com.bot4s.zmatrix.services.Authentication
import sttp.client3.httpclient.zio.HttpClientZioBackend

trait ExampleApp[T] extends zio.ZIOAppDefault {

  def runExample: ZIO[AuthMatrixEnv with Matrix, MatrixError, T]

  override def run: ZIO[Environment, Any, ExitCode] =
    (Authentication.refresh *> runExample.withAutoRefresh.retry(Schedule.recurs(5)))
      .tapError(error => ZIO.logError(error.toString()))
      .exitCode
      .provide(
        SyncTokenConfiguration
          .persistent()
          .mapError(x => new Exception(s"Unable to read token configuration $x"))
          .orDie,
        MatrixConfiguration.live().mapError(x => new Exception(s"Unable to read configuration $x")).orDie,
        Authentication.live,
        HttpClientZioBackend.layer().orDie,
        MatrixClient.live,
        Matrix.make
      )

}

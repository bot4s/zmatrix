package com.bot4s.zmatrix

import zio.{ Schedule, _ }

import com.bot4s.zmatrix.client.MatrixClient
import com.bot4s.zmatrix.services.Authentication
import sttp.client3.httpclient.zio.HttpClientZioBackend

trait ExampleApp[T] extends ZIOAppDefault {

  def runExample: ZIO[AuthMatrixEnv, MatrixError, T]

  override def run: ZIO[Environment, Throwable, ExitCode] =
    (Authentication.refresh *> runExample.withAutoRefresh.retry(Schedule.recurs(5)))
      .tapError(error => ZIO.logError(error.toString()))
      .exitCode
      .provide(
        SyncTokenConfiguration.persistent(),
        MatrixConfiguration.live(),
        Authentication.live,
        HttpClientZioBackend.layer(),
        MatrixClient.live,
        Matrix.make
      )

}

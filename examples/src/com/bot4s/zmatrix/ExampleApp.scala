package com.bot4s.zmatrix

import zio.{ Schedule, _ }

import com.bot4s.zmatrix.client.MatrixClient
import com.bot4s.zmatrix.services.Authentication
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.SttpBackend

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
        // On scala 3, the type of the ZEnvironment is not happy
        HttpClientZioBackend.layer(): TaskLayer[SttpBackend[Task, Any]],
        MatrixClient.live,
        Matrix.make
      )

}

package com.bot4s.zmatrix

import zio.Console._
import com.bot4s.zmatrix.api.{ accounts, roomMembership }
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.client.MatrixClient
import zio.{ ExitCode, URIO, ZEnv, ZIO }

import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import com.bot4s.zmatrix.services.Authentication

trait ExampleApp extends zio.App {

  def runExample(args: List[String]): URIO[AuthMatrixEnv, ExitCode]

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = runExample(args)
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

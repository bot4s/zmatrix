package com.bot4s.zmatrix

import zio.magic._
import zio.console._
import com.bot4s.zmatrix.api.{ accounts, roomMembership }
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.client.MatrixClient
import zio.{ ExitCode, URIO, ZEnv, ZIO }

import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import com.bot4s.zmatrix.services.Authentication
import com.bot4s.zmatrix.services.Logger

trait ExampleApp extends zio.App {

  def runExample(args: List[String]): URIO[AuthMatrixEnv, ExitCode]

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = runExample(args)
    .inject(
      ZEnv.live,
      Logger.live("matrix-zio-main"),
      MatrixTokenConfiguration
        .persistent()
        .mapError(x => new Exception(s"Unable to read token configuration $x"))
        .orDie,
      MatrixConfiguration.live().mapError(x => new Exception(s"Unable to read configuration $x")).orDie,
      Authentication.live,
      AsyncHttpClientZioBackend.layer().orDie,
      MatrixClient.live
    )

}

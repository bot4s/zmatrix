package com.bot4s.zmatrix

import zio._
import zio.Console._
import com.bot4s.zmatrix.api.{ accounts, roomMembership }

object Simple extends ExampleApp[Unit] {

  override def runExample: ZIO[AuthMatrixEnv, MatrixError, Unit] =
    (accounts.whoAmI *> roomMembership.joinedRooms())
      .tapError(e => printLineError(e.toString()))
      .flatMap(x => printLine(x.toString()))
      .refineOrDie { case x: MatrixError => x }

}

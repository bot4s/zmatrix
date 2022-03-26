package com.bot4s.zmatrix

import com.bot4s.zmatrix.api.{ accounts, roomMembership }
import zio.Console._
import zio.{ ExitCode, URIO }

object Simple extends ExampleApp {

  override def runExample(args: List[String]): URIO[AuthMatrixEnv, ExitCode] =
    (accounts.whoAmI <*> roomMembership.joinedRooms())
      .tapError(e => putStrLn(e.toString()))
      .flatMap(x => putStrLn(x.toString()))
      .exitCode

}

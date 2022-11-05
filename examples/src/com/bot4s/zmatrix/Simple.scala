package com.bot4s.zmatrix

import zio.Console._

import com.bot4s.zmatrix.api.{ accounts, roomMembership }

object Simple extends ExampleApp[Unit] {

  val runExample =
    (for {
      _     <- accounts.whoAmI.debug
      rooms <- roomMembership.joinedRooms()
      _     <- printLine(f"I'm a member of all those rooms: ${rooms}")
    } yield ())
      .tapError(e => printLineError(e.toString()))
      .refineOrDie { case x: MatrixError => x }

}

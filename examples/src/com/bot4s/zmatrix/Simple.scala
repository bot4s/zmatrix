package com.bot4s.zmatrix

import zio.Console._

import com.bot4s.zmatrix.api.{accounts, roomMembership}

object Simple extends ExampleApp[Unit] {

  val runExample =
    (accounts.whoAmI *> roomMembership.joinedRooms())
      .tapError(e => printLineError(e.toString()))
      .flatMap(x => printLine(f"I'm a member of all those rooms: ${x.toString()}"))
      .refineOrDie { case x: MatrixError => x }
      .unit

}

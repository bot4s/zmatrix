package com.bot4s.zmatrix

import zio.Console._

object Simple extends ExampleApp[Unit] {

  val runExample =
    (for {
      _     <- Matrix.whoAmI.debug
      rooms <- Matrix.joinedRooms
      _     <- printLine(f"I'm a member of all those rooms: ${rooms}\n")
    } yield ())
      .tapError(e => printLineError(e.toString()))
      .refineOrDie { case x: MatrixError => x }

}

package com.bot4s.zmatrix

import zio.Console._
import zio._

import com.bot4s.zmatrix.api.{ accounts, logout }

object ClearSessions extends ExampleApp[Unit] {

  val runExample =
    (for {
      currrent <- accounts.whoAmI
      validation <- readLine(s"Do you want to remove all sessions for ${currrent.userId} ? (Y/N): ")
                      .repeatWhile(entry => !Set("y", "n").contains(entry.toLowerCase()))
      _ <- ZIO.when(validation.toLowerCase.startsWith("y"))(printLine("Clearing all sessions\n") *> logout.logoutAll)
    } yield ())
      .tapError(e => printLineError(e.toString()))
      .refineOrDie { case x: MatrixError => x }

}

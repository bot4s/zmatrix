package com.bot4s.zmatrix

import zio.Console._
import zio._

object Runner extends ZIOAppDefault {

  private def examples = Map(
    "Simple"        -> Simple,
    "Upload"        -> Upload,
    "ImageMessage"  -> ImageMessage,
    "SimpleSync"    -> SimpleSync,
    "CreateRoom"    -> CreateRoom,
    "ClearSessions" -> ClearSessions
  )

  def run = {
    val examplesStr =
      examples.keySet.mkString(start = "Available examples (ctrl-c to exit):\n\t", sep = "\n\t", end = "\n> ")
    (for {
      _     <- print(examplesStr)
      input <- readLine
      example <- ZIO
                   .fromOption(examples.get(input))
                   .mapError(_ => new Exception(s"Example '$input' does not exist"))
                   .tapError(e => printLineError(e.getMessage()))
      runnable <- example.run
    } yield runnable).retry(Schedule.forever).repeat(Schedule.forever)
  }

}

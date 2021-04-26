package com.bot4s.zmatrix

import zio.console._
import zio.{ ExitCode, URIO, ZEnv, ZIO }

object Runner extends zio.App {

  private def examples = Map(
    "Simple"     -> Simple,
    "SimpleSync" -> SimpleSync,
    "CreateRoom" -> CreateRoom
  )

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val examplesStr = examples.keySet.mkString(start = "Available examples:\n\t", sep = "\n\t", end = "\n> ")
    (for {
      _        <- putStr(examplesStr)
      input    <- getStrLn
      example  <- ZIO.fromOption(examples.get(input)).mapError(_ => new Exception(s"$input does not exist"))
      runnable <- example.run(List())
    } yield runnable).orDie
  }

}

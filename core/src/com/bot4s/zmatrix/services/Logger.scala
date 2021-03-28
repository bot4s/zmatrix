package com.bot4s.zmatrix.services

import zio.logging.{ LogFormat, LogLevel, Logging }

object Logger {

  def live(name: String) = Logging.console(
    logLevel = LogLevel.Info,
    format = LogFormat.ColoredLogFormat()
  ) >>> Logging.withRootLoggerName(name)
}

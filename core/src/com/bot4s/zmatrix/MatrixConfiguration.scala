package com.bot4s.zmatrix

import zio.{ Has, IO, Layer, Ref, UIO, URIO, ZIO }
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderFailures

final case class Config(
  matrix: MatrixConfigurationContent
)
final case class MatrixConfigurationContent(
  homeServer: String,
  apiPrefix: String = MatrixConfiguration.DEFAULT_API_PREFIX,
  userId: Option[String] = None,
  deviceName: Option[String] = None,
  deviceId: Option[String] = None
) {
  val apiPath = f"${homeServer}${apiPrefix}"
}

/**
 */
trait MatrixConfiguration {
  def get: UIO[Config]
}

object MatrixConfiguration {

  val DEFAULT_API_PREFIX  = "/_matrix/client/r0"
  val DEFAULT_CONFIG_FILE = "bot.conf"

  def get: URIO[Has[MatrixConfiguration], Config] = ZIO.accessM(_.get.get)

  private def refFromFile(filename: String): IO[ConfigReaderFailures, Ref[Config]] =
    ZIO.fromEither(ConfigSource.resources(filename).load[Config]) >>= Ref.make

  /**
   * Create an in-memory configuration that is not persistent.
   */
  def live(filename: String = DEFAULT_CONFIG_FILE): Layer[ConfigReaderFailures, Has[MatrixConfiguration]] =
    refFromFile(filename).map { configRef =>
      new MatrixConfiguration {
        override def get: UIO[Config] = configRef.get
      }
    }.toLayer

}

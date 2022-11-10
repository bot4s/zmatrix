package com.bot4s.zmatrix

import zio._

import pureconfig._
import pureconfig.error.ConfigReaderFailures

final case class Config(
  matrix: MatrixConfigurationContent
)
final case class MatrixConfigurationContent(
  homeServer: String,
  apiPrefix: String = MatrixConfiguration.DEFAULT_API_PREFIX,
  apiVersion: String = MatrixConfiguration.DEFAULT_API_VERSION,
  userId: Option[String] = None,
  deviceName: Option[String] = None,
  deviceId: Option[String] = None
) {
  val clientApi = f"${homeServer}${apiPrefix}/client/${apiVersion}"
  val mediaApi  = f"${homeServer}${apiPrefix}/media/${apiVersion}"
}

/**
 */
trait MatrixConfiguration {
  def get: UIO[Config]
}

object MatrixConfiguration {

  val DEFAULT_API_PREFIX  = "/_matrix"
  val DEFAULT_API_VERSION = "v3"
  val DEFAULT_CONFIG_FILE = "bot.conf"

  private implicit val matrixContentConfigReader: ConfigReader[MatrixConfigurationContent] =
    ConfigReader.forProduct6("home-server", "api-prefix", "api-version", "user-id", "device-name", "device-id")(
      MatrixConfigurationContent(_, _, _, _, _, _)
    )
  private implicit val configReader: ConfigReader[Config] = ConfigReader.forProduct1("matrix")(Config(_))

  def get: URIO[MatrixConfiguration, Config] = ZIO.serviceWithZIO(_.get)

  private def refFromFile(filename: String): IO[ConfigReaderFailures, Ref[Config]] =
    ZIO.fromEither(ConfigSource.resources(filename).load[Config]).flatMap(e => Ref.make(e))

  /**
   * Create an in-memory configuration that is not persistent.
   */
  def live(filename: String = DEFAULT_CONFIG_FILE): Layer[ConfigReaderFailures, MatrixConfiguration] =
    ZLayer.fromZIO(refFromFile(filename).map { configRef =>
      new MatrixConfiguration {
        override def get: UIO[Config] = configRef.get
      }
    })

}

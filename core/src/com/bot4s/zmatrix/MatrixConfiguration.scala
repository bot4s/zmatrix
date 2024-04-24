package com.bot4s.zmatrix

import zio._
import zio.config._
import zio.config.magnolia._
import zio.config.typesafe._

import java.io.{ File, FileNotFoundException }
import java.net.URL

import com.typesafe.config.{ ConfigFactory, ConfigParseOptions }

final case class MatrixConfiguration(
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

object MatrixConfiguration {

  val DEFAULT_API_PREFIX  = "/_matrix"
  val DEFAULT_API_VERSION = "v3"
  val DEFAULT_CONFIG_FILE = "bot.conf"

  val configReader = deriveConfig[MatrixConfiguration]

  def from(filename: String): Task[MatrixConfiguration] =
    fromFile(filename)
      .orElse(fromResource(filename))

  private[this] val strictSettings =
    ConfigParseOptions.defaults.setAllowMissing(false)

  private def fromHoconFile(url: URL) =
    ZIO
      .attempt(ConfigFactory.parseURL(url, strictSettings.setClassLoader(null)))
      .flatMap(config =>
        read(
          configReader from ConfigProvider.fromTypesafeConfig(config)
        )
      )

  private def fromResource(filename: String) = {
    val adapted = if (filename.startsWith("/")) filename else s"/$filename"
    for {
      file <- ZIO
                .attempt(getClass.getResource(adapted).toURI().toURL())
                .mapError(_ => new FileNotFoundException(s"Unable to find file '$filename'"))
      config <- fromHoconFile(file)
    } yield config
  }

  private def fromFile(filename: String) =
    for {
      file <- ZIO
                .attempt(new File(filename).toURI().toURL())
                .mapError(_ => new FileNotFoundException(s"Unable to find file '$filename'"))
      config <- fromHoconFile(file)
    } yield config

  def live(filename: String = DEFAULT_CONFIG_FILE): TaskLayer[MatrixConfiguration] =
    ZLayer.fromZIO(from(filename))

}

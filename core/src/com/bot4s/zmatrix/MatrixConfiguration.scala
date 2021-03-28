package com.bot4s.zmatrix

import zio.{ Has, IO, Layer, Managed, Ref, UIO, URIO, ZIO }
import pureconfig.{ ConfigSource, ConfigWriter }
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderFailures
import java.io.{ BufferedWriter, File, FileWriter }
import com.typesafe.config.ConfigRenderOptions

final case class Config(
  matrix: MatrixConfigurationContent
)
final case class MatrixConfigurationContent(
  homeServer: String,
  apiPrefix: String = MatrixConfiguration.DEFAULT_API_PREFIX,
  userId: Option[String] = None,
  deviceName: Option[String] = None,
  deviceId: Option[String] = None,
  since: Option[String] = None
) {
  val apiPath = f"${homeServer}${apiPrefix}"
}

/**
 */
trait MatrixConfiguration {
  def get: UIO[Config]
  def set(config: Config): UIO[Unit]
}

object MatrixConfiguration {

  val DEFAULT_API_PREFIX  = "/_matrix/client/r0"
  val DEFAULT_CONFIG_FILE = "bot.conf"

  def get: URIO[Has[MatrixConfiguration], Config]               = ZIO.accessM(_.get.get)
  def set(config: Config): URIO[Has[MatrixConfiguration], Unit] = ZIO.accessM(_.get.set(config))

  private def refFromFile(filename: String): IO[ConfigReaderFailures, Ref[Config]] =
    ZIO.fromEither(ConfigSource.resources(filename).load[Config]) >>= Ref.make

  /**
   * Create an in-memory configuration that is not persistent.
   * This is not recommended as the pagination `since` token will not be kept between two runs.
   */
  def live(filename: String = DEFAULT_CONFIG_FILE): Layer[ConfigReaderFailures, Has[MatrixConfiguration]] =
    refFromFile(filename).map { configRef =>
      new MatrixConfiguration {
        override def get: UIO[Config]               = configRef.get
        override def set(config: Config): UIO[Unit] = configRef.set(config)
      }
    }.toLayer

  /**
   * Create a persistent (on disk storage) configuration from the given file.
   * This layer, when updated, will write back its changes in the given configuration file
   */
  def persistent(filename: String = DEFAULT_CONFIG_FILE): Layer[ConfigReaderFailures, Has[MatrixConfiguration]] =
    refFromFile(filename).map { configRef =>
      new MatrixConfiguration {
        override def get: UIO[Config] = configRef.get
        override def set(config: Config): UIO[Unit] = {
          val renderOptions = ConfigRenderOptions.concise().setFormatted(true).setJson(false)
          val toWrite       = ConfigWriter[Config].to(config).render(renderOptions)

          val updateConf = for {
            uri    <- ZIO.fromOption(Option(getClass().getClassLoader().getResource(filename)))
            file    = new File(uri.toURI())
            managed = Managed.make(ZIO.effect(new BufferedWriter(new FileWriter(file))))(bw => IO.succeed(bw.close))
            _      <- configRef.set(config)
            _      <- managed.use(c => IO.effect(c.write(toWrite)))
          } yield ()

          updateConf.catchAll(_ => IO.succeed(()))
        }
      }
    }.toLayer

}

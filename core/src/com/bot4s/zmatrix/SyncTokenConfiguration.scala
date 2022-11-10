package com.bot4s.zmatrix

import zio._

import java.io.{ BufferedWriter, File, FileWriter }

import com.typesafe.config.ConfigRenderOptions
import pureconfig.error.ConfigReaderFailures
import pureconfig._

final case class SyncToken(
  since: Option[String] = None
)

trait SyncTokenConfiguration {
  def get: UIO[SyncToken]
  def set(config: SyncToken): UIO[Unit]
}

object SyncTokenConfiguration {
  val DEFAULT_TOKEN_FILE = "token.conf"

  def get: URIO[SyncTokenConfiguration, SyncToken]               = ZIO.serviceWithZIO(_.get)
  def set(config: SyncToken): URIO[SyncTokenConfiguration, Unit] = ZIO.serviceWithZIO(_.set(config))

  private implicit val tokenConfReader: ConfigReader[SyncToken] = ConfigReader.forProduct1("since")(SyncToken(_))
  private implicit val tokenConfigWriter: ConfigWriter[SyncToken] =
    ConfigWriter[Option[String]].contramap[SyncToken](_.since)

  private def refFromFile(filename: String): IO[ConfigReaderFailures, Ref[SyncToken]] =
    ZIO
      .fromEither(ConfigSource.file(filename).load[SyncToken])
      .orElse(ZIO.succeed(SyncToken(None)))
      .flatMap(token => Ref.make(token))

  /**
   * Create an in-memory configuration that is not persistent.
   * It's not recommended to use this layer as the token will not be persisted
   * between runs
   */
  def live(filename: String = DEFAULT_TOKEN_FILE): Layer[ConfigReaderFailures, SyncTokenConfiguration] =
    ZLayer.fromZIO(refFromFile(filename).map { tokenRef =>
      new SyncTokenConfiguration {
        def get: UIO[SyncToken]               = tokenRef.get
        def set(config: SyncToken): UIO[Unit] = tokenRef.set(config)
      }
    })

  /**
   * Create a persistent (on disk storage) configuration from the given file.
   * This layer, when updated, will write back its changes in the given configuration file
   */
  def persistent(
    filename: String = DEFAULT_TOKEN_FILE
  ): Layer[ConfigReaderFailures, SyncTokenConfiguration] =
    ZLayer.fromZIO(refFromFile(filename).map { configRef =>
      new SyncTokenConfiguration {
        override def get: UIO[SyncToken] = configRef.get
        override def set(config: SyncToken): UIO[Unit] = {
          val renderOptions = ConfigRenderOptions.concise().setFormatted(true).setJson(false)
          val toWrite       = ConfigWriter[SyncToken].to(config).render(renderOptions)

          val updateConf = for {
            file <- ZIO.attempt(new File(filename))
            _    <- configRef.set(config)
            _ <-
              ZIO.acquireReleaseWith(ZIO.attempt(new BufferedWriter(new FileWriter(file))))(bw =>
                ZIO.succeed(bw.close)
              )(c => ZIO.attempt(c.write(toWrite)))
          } yield ()

          updateConf.catchAll(_ => ZIO.succeed(()))
        }
      }
    })

}

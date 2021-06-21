package com.bot4s.zmatrix

import zio.{ Has, IO, Layer, Managed, Ref, UIO, URIO, ZIO }
import pureconfig.{ ConfigSource, ConfigWriter }
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderFailures
import java.io.{ BufferedWriter, File, FileWriter }
import com.typesafe.config.ConfigRenderOptions

final case class SyncToken(
  since: Option[String] = None
)

trait SyncTokenConfiguration {
  def get: UIO[SyncToken]
  def set(config: SyncToken): UIO[Unit]
}

object SyncTokenConfiguration {
  val DEFAULT_TOKEN_FILE = "token.conf"

  def get: URIO[Has[SyncTokenConfiguration], SyncToken]               = ZIO.accessM(_.get.get)
  def set(config: SyncToken): URIO[Has[SyncTokenConfiguration], Unit] = ZIO.accessM(_.get.set(config))

  private def refFromFile(filename: String): IO[ConfigReaderFailures, Ref[SyncToken]] =
    ZIO
      .fromEither(ConfigSource.file(filename).load[SyncToken])
      .orElse(ZIO.succeed(SyncToken(None))) >>= Ref.make

  /**
   * Create an in-memory configuration that is not persistent.
   * It's not recommended to use this layer as the token will not be persisted
   * between runs
   */
  def live(filename: String = DEFAULT_TOKEN_FILE): Layer[ConfigReaderFailures, Has[SyncTokenConfiguration]] =
    refFromFile(filename).map { tokenRef =>
      new SyncTokenConfiguration {
        def get: UIO[SyncToken]               = tokenRef.get
        def set(config: SyncToken): UIO[Unit] = tokenRef.set(config)
      }
    }.toLayer

  /**
   * Create a persistent (on disk storage) configuration from the given file.
   * This layer, when updated, will write back its changes in the given configuration file
   */
  def persistent(
    filename: String = DEFAULT_TOKEN_FILE
  ): Layer[ConfigReaderFailures, Has[SyncTokenConfiguration]] =
    refFromFile(filename).map { configRef =>
      new SyncTokenConfiguration {
        override def get: UIO[SyncToken] = configRef.get
        override def set(config: SyncToken): UIO[Unit] = {
          val renderOptions = ConfigRenderOptions.concise().setFormatted(true).setJson(false)
          val toWrite       = ConfigWriter[SyncToken].to(config).render(renderOptions)

          val updateConf = for {
            file   <- ZIO.effect(new File(filename))
            managed = Managed.make(ZIO.effect(new BufferedWriter(new FileWriter(file))))(bw => IO.succeed(bw.close))
            _      <- configRef.set(config)
            _      <- managed.use(c => IO.effect(c.write(toWrite)))
          } yield ()

          updateConf.catchAll(_ => IO.succeed(()))
        }
      }
    }.toLayer

}

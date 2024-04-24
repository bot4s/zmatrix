package com.bot4s.zmatrix

import zio._
import zio.config._
import zio.config.magnolia._
import zio.config.typesafe._

import java.io.{ BufferedWriter, File, FileWriter }

final case class SyncToken(
  since: Option[String] = None
)

trait SyncTokenConfiguration {
  def get: UIO[SyncToken]
  def set(config: SyncToken): UIO[Unit]
}

object SyncTokenConfiguration {
  val DEFAULT_TOKEN_FILE = "since.conf"

  def get: URIO[SyncTokenConfiguration, SyncToken]               = ZIO.serviceWithZIO(_.get)
  def set(config: SyncToken): URIO[SyncTokenConfiguration, Unit] = ZIO.serviceWithZIO(_.set(config))

  val configReader = deriveConfig[SyncToken]

  private def refFromFile(filename: String): Task[Ref[SyncToken]] =
    for {
      file   <- ZIO.attempt(new File(filename))
      source  = ConfigProvider.fromHoconFile(file)
      config <- read(configReader from source)
      result <- Ref.make(config)
    } yield result

  /**
   * Create an in-memory configuration that is not persistent.
   * It's not recommended to use this layer as the token will not be persisted
   * between runs
   */
  val live: TaskLayer[SyncTokenConfiguration] = ZLayer {
    Ref.make(SyncToken(None)).map { t =>
      new SyncTokenConfiguration {
        def get: UIO[SyncToken] = t.get

        def set(config: SyncToken): UIO[Unit] = t.set(config)

      }
    }
  }

  /* Use the token stored in a file and store it in memory.
   * This is not persistent and is mostly useful to replay the bot
   * with an older "since" token
   */
  def liveFromFile(filename: String = DEFAULT_TOKEN_FILE): TaskLayer[SyncTokenConfiguration] =
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
  ): TaskLayer[SyncTokenConfiguration] =
    ZLayer.fromZIO(refFromFile(filename).map { configRef =>
      new SyncTokenConfiguration {
        override def get: UIO[SyncToken] = configRef.get
        override def set(config: SyncToken): UIO[Unit] = {

          val updateConf = for {
            _    <- configRef.set(config)
            file <- ZIO.attempt(new File(filename))
            // zio-config 4.X removed the ability to write a config
            content = s"""since="${config.since.mkString}""""
            _ <-
              ZIO.acquireReleaseWith(ZIO.attempt(new BufferedWriter(new FileWriter(file))))(bw =>
                ZIO.succeed(bw.close)
              )(c => ZIO.attempt(c.write(content)))
          } yield ()

          updateConf.catchAll(_ => ZIO.succeed(()))
        }
      }
    })

}

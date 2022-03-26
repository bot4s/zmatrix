package com.bot4s.zmatrix.services

import com.bot4s.zmatrix._
import zio.{ IO, Ref, UIO, URIO, ZIO }
import com.bot4s.zmatrix.models.AccessToken
import com.bot4s.zmatrix.api.login
import com.bot4s.zmatrix.MatrixError
import zio.ZLayer

/**
 * Authentication service responsible to store and refresh the access token
 * required to access Matrix' API
 */
trait Authentication {
  def accessToken: UIO[AccessToken]
  def refresh: IO[MatrixError, AccessToken]
}

object Authentication {

  def accessToken: URIO[Authentication, AccessToken]         = ZIO.environmentWithZIO(_.get.accessToken)
  def refresh: ZIO[Authentication, MatrixError, AccessToken] = ZIO.environmentWithZIO(_.get.refresh)

  /**
   * Default implementation for the Authentiction service, it will use the  MATRIX_BOT_ACCESS and MATRIX_BOT_PASSWORD
   * from the environment variable to create or update an access token.
   * The refresh method can be used  to re-create a token from a password, this can be useful for the first login but
   * it is not a good idea to create a new access token at each restart, it should be store somewhere safe between runs.
   */
  val live = ZLayer.fromZIO(
    ZIO
      .environmentWithZIO[MatrixEnv] { env =>
        env.get[MatrixConfiguration].get.flatMap { config =>
          Ref.make(AccessToken(sys.env.getOrElse("MATRIX_BOT_ACCESS", ""))).map { tokenRef =>
            new Authentication {
              def accessToken: UIO[AccessToken] = tokenRef.get

              def refresh: IO[MatrixError, AccessToken] =
                (config.matrix.userId, sys.env.get("MATRIX_BOT_PASSWORD")) match {
                  case (Some(userId), Some(password)) =>
                    login
                      .passwordLogin(
                        user = userId,
                        password = password,
                        deviceId = config.matrix.deviceId
                      )
                      .flatMap(response => tokenRef.updateAndGet(_ => response.accessToken))
                      .provideEnvironment(env)
                  case (Some(_), _) =>
                    ZIO.fail(
                      MatrixError.InvalidParameterError("password", "Missing password, please set MATRIX_BOT_PASSWORD")
                    )
                  case (None, _) =>
                    ZIO.fail(MatrixError.InvalidParameterError("userId", "user-id is not defined in configuration"))
                }
            }
          }
        }
      }
  )
}

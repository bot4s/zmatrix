package com.bot4s.zmatrix.api

import zio.ZIO
import com.bot4s.zmatrix.MatrixError
import com.bot4s.zmatrix._

trait SessionManagement {

  /*
   * Invalidate the current access token
   * Documentation: https://matrix.org/docs/spec/client_server/latest#post-matrix-client-r0-logout
   */
  def logout(): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Unit](post(Seq("logout")))

  /*
   * Delete all existing tokens for the user. The current token will be invalidated
   * Documentation: https://matrix.org/docs/spec/client_server/latest#post-matrix-client-r0-logout-all
   */
  def logoutAll(): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Unit](post(Seq("logout", "all")))
}

object sessions extends SessionManagement

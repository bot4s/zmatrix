package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.{ Matrix, MatrixApiBase }

trait Logout { self: MatrixApiBase =>

  /*
   * Invalidate the current access token
   * Documentation: https://matrix.org/docs/spec/client_server/latest#post-matrix-client-r0-logout
   */
  def logout =
    sendWithAuth[Unit](post(Seq("logout")))

  /*
   * Delete all existing tokens for the user. The current token will be invalidated
   * Documentation: https://matrix.org/docs/spec/client_server/latest#post-matrix-client-r0-logout-all
   */
  def logoutAll =
    sendWithAuth[Unit](post(Seq("logout", "all")))
}

private[zmatrix] trait LogoutAccessors {
  def logout    = ZIO.serviceWithZIO[Matrix](_.logout)
  def logoutAll = ZIO.serviceWithZIO[Matrix](_.logoutAll)
}

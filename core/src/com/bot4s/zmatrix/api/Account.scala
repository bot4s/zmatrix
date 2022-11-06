package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.{ AuthMatrixEnv, Matrix, MatrixApiBase, MatrixError }

trait Account { self: MatrixApiBase =>
  def whoAmI: ZIO[AuthMatrixEnv, MatrixError, UserResponse] =
    sendWithAuth[UserResponse](get(Seq("account", "whoami")))
}

private[zmatrix] trait AccountAccessors {
  def whoAmI: ZIO[AuthMatrixEnv, MatrixError, UserResponse] =
    ZIO.serviceWithZIO[Matrix](_.whoAmI)
}

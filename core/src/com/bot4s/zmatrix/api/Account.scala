package com.bot4s.zmatrix.api

import zio.ZIO
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }

trait Account {
  def whoAmI: ZIO[AuthMatrixEnv, MatrixError, UserResponse] =
    sendWithAuth[UserResponse](get(Seq("account", "whoami")))
}

object accounts extends Account

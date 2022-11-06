package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.models.responses.SyncState
import com.bot4s.zmatrix.{ AuthMatrixEnv, Matrix, MatrixApiBase, MatrixError }

trait Sync { self: MatrixApiBase =>
  def sync: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
    for {
      request <- withSince(get(Seq("sync")))
      result  <- sendWithAuth[SyncState](request)
    } yield result
}

private[zmatrix] trait SyncAccessors {
  def sync = ZIO.serviceWithZIO[Matrix](_.sync)
}

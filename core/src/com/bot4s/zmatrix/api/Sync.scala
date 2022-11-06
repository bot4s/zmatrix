package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.models.responses.SyncState
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }

trait Sync {

  def sync: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
    for {
      request <- withSince(get(Seq("sync")))
      result  <- sendWithAuth[SyncState](request)
    } yield result

}

object sync extends Sync

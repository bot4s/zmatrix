package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.models.responses.SyncState
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }

trait Sync {

  def sync: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
    for {
      request <- get(Seq("sync"))
      request <- withSince(request)
      request <- authenticate(request)
      result  <- send(request)
      decoded <- as[SyncState](result)
    } yield decoded

}

object sync extends Sync

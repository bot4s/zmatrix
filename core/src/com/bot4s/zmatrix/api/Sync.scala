package com.bot4s.zmatrix.api

import zio.ZIO
import com.bot4s.zmatrix.models.StateDecoder._
import com.bot4s.zmatrix.models.SyncState
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }

trait Sync {

  def sync: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
    for {
      originalReq <- get(Seq("sync"))
      withToken   <- withSince(originalReq)
      withAuth    <- authenticate(withToken)
      result      <- send(withAuth)
      decoded     <- as[SyncState](result)
    } yield decoded

}

object sync extends Sync

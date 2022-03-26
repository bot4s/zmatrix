package com.bot4s.zmatrix.api

import zio.ZIO
import com.bot4s.zmatrix.models.StateDecoder._
import com.bot4s.zmatrix.models.SyncState
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }

trait Sync {

  def sync: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
    sendWithAuth[SyncState](get(Seq("sync")))

}

object sync extends Sync

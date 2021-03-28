package com.bot4s.zmatrix.api

import zio.ZIO
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }
import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.models.responses._

trait RoomMembership {
  def join(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, JoinResponse] =
    (post(Seq("join", roomId.id)) >>= authenticate >>= send) >>= as[JoinResponse]

  def joinedRooms(): ZIO[AuthMatrixEnv, MatrixError, List[String]] =
    (get(Seq("joined_rooms")) >>= authenticate >>= send).flatMap(json =>
      as(json)(_.downField("joined_rooms").as[List[String]])
    )

}

object roomMembership extends RoomMembership

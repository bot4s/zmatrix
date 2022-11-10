package com.bot4s.zmatrix.api

import zio.ZIO
import zio.json.ast._

import com.bot4s.zmatrix.models.{ RoomCreationData, RoomId }
import com.bot4s.zmatrix.{ AuthMatrixEnv, Matrix, MatrixApiBase, MatrixError }

trait RoomCreation { self: MatrixApiBase =>

  /**
   * Create a room on a matrix server
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-createroom
   */
  def createRoom(roomCreation: RoomCreationData): ZIO[AuthMatrixEnv, MatrixError, RoomId] =
    sendWithAuth[RoomId](postJson(Seq("createRoom"), roomCreation))(
      Json.decoder.mapOrFail(_.get(JsonCursor.field("room_id")).flatMap(_.as[RoomId]))
    )

}

private[zmatrix] trait RoomCreationAccessors {
  def createRoom(roomCreation: RoomCreationData) = ZIO.serviceWithZIO[Matrix](_.createRoom(roomCreation))
}

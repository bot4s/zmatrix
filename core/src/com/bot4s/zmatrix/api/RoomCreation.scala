package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.models.{ RoomCreationData, RoomId }
import com.bot4s.zmatrix.{ AuthMatrixEnv, Matrix, MatrixApiBase, MatrixError }
import io.circe.syntax._

trait RoomCreation { self: MatrixApiBase =>

  /**
   * Create a room on a matrix server
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-createroom
   */
  def createRoom(roomCreation: RoomCreationData): ZIO[AuthMatrixEnv, MatrixError, RoomId] =
    sendWithAuth(postJson(Seq("createRoom"), roomCreation.asJson.dropNullValues))(_.downField("room_id").as[RoomId])

}

private[zmatrix] trait RoomCreationAccessors {
  def createRoom(roomCreation: RoomCreationData) = ZIO.serviceWithZIO[Matrix](_.createRoom(roomCreation))
}

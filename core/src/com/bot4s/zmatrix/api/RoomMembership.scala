package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.models.RoomId
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }
import io.circe.Json

trait RoomMembership {

  /**
   * Join a given room
   * Documentation:https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-join
   */
  def join(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, JoinResponse] =
    sendWithAuth[JoinResponse](post(Seq("join", roomId.id)))

  /**
   * Get a list of joined rooms
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-joined-rooms
   */
  def joinedRooms(): ZIO[AuthMatrixEnv, MatrixError, List[RoomId]] =
    sendWithAuth[List[RoomId]](get(Seq("joined_rooms")))(_.downField("joined_rooms").as[List[RoomId]])

  /**
   * Invite a user in the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-invite
   */
  def invite(roomId: RoomId, user: String): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Unit](postJson(Seq("rooms", roomId.id, "invite"), Json.obj("user_id" -> Json.fromString(user))))

  /**
   * Forget the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-forget
   */
  def forget(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Unit](
      post(
        Seq("rooms", roomId.id, "forget")
      )
    )

  /**
   * Leave the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-leave
   */
  def leave(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Unit](
      post(
        Seq("rooms", roomId.id, "leave")
      )
    )

  /**
   * Ban the user from a  given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-ban
   */
  def ban(roomId: RoomId, user: String, reason: Option[String] = None): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Unit](
      postJson(
        Seq("rooms", roomId.id, "ban"),
        Json.obj(
          "user_id" -> Json.fromString(user),
          "reason"  -> Json.fromString(reason.getOrElse(""))
        )
      )
    )

  /**
   * Uban the user from a given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-unban
   */
  def unban(roomId: RoomId, user: String): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Unit](
      postJson(
        Seq("rooms", roomId.id, "unban"),
        Json.obj(
          "user_id" -> Json.fromString(user)
        )
      )
    )

  /**
   * Kick the user from a  given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-kick
   */
  def kick(roomId: RoomId, user: String, reason: Option[String] = None): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Unit](
      postJson(
        Seq("rooms", roomId.id, "kick"),
        Json.obj(
          "user_id" -> Json.fromString(user),
          "reason"  -> Json.fromString(reason.getOrElse(""))
        )
      )
    )

}

object roomMembership extends RoomMembership

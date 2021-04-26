package com.bot4s.zmatrix.api

import zio.ZIO
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.models.RoomId
import io.circe.Json
import zio.logging.log
import com.bot4s.zmatrix.models.StateDecoder._

trait RoomMembership {

  /**
   * Join a given room
   * Documentation:https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-join
   */
  def join(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, JoinResponse] =
    post(Seq("join", roomId.id)) >>= authenticate >>= send >>= as[JoinResponse]

  /**
   * Get a list of joined rooms
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-joined-rooms
   */
  def joinedRooms(): ZIO[AuthMatrixEnv, MatrixError, List[RoomId]] =
    (get(Seq("joined_rooms")) >>= authenticate >>= send).flatMap(json =>
      as(json)(_.downField("joined_rooms").as[List[RoomId]])
    )

  /**
   * Invite a user in the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-invite
   */
  def invite(roomId: RoomId, user: String): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    postJson(
      Seq("rooms", roomId.id, "invite"),
      Json.obj("user_id" -> Json.fromString(user))
    ).flatMap(x => authenticate(x).tap(x => log.info(x.toCurl))) >>= send >>= as[Unit]

  /**
   * Forget the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-forget
   */
  def forget(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    post(
      Seq("rooms", roomId.id, "forget")
    ) >>= authenticate >>= send >>= as[Unit]

  /**
   * Leave the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-leave
   */
  def leave(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    post(
      Seq("rooms", roomId.id, "leave")
    ) >>= authenticate >>= send >>= as[Unit]

  /**
   * Ban the user from a  given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-ban
   */
  def ban(roomId: RoomId, user: String, reason: Option[String] = None): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    postJson(
      Seq("rooms", roomId.id, "ban"),
      Json.obj(
        "user_id" -> Json.fromString(user),
        "reason"  -> Json.fromString(reason.getOrElse(""))
      )
    ) >>= authenticate >>= send >>= as[Unit]

  /**
   * Uban the user from a given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-unban
   */
  def unban(roomId: RoomId, user: String): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    postJson(
      Seq("rooms", roomId.id, "unban"),
      Json.obj(
        "user_id" -> Json.fromString(user)
      )
    ) >>= authenticate >>= send >>= as[Unit]

  /**
   * Kick the user from a  given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-kick
   */
  def kick(roomId: RoomId, user: String, reason: Option[String] = None): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    postJson(
      Seq("rooms", roomId.id, "kick"),
      Json.obj(
        "user_id" -> Json.fromString(user),
        "reason"  -> Json.fromString(reason.getOrElse(""))
      )
    ) >>= authenticate >>= send >>= as[Unit]

}

object roomMembership extends RoomMembership

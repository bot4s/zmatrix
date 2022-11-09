package com.bot4s.zmatrix.api

import zio.ZIO
import zio.json.ast._

import com.bot4s.zmatrix.models.RoomId
import com.bot4s.zmatrix.models.responses._
import com.bot4s.zmatrix.{ AuthMatrixEnv, Matrix, MatrixApiBase, MatrixError }

trait RoomMembership { self: MatrixApiBase =>

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
  def joinedRooms: ZIO[AuthMatrixEnv, MatrixError, List[RoomId]] =
    sendWithAuth[List[RoomId]](get(Seq("joined_rooms")))(
      Json.decoder.mapOrFail(_.get(JsonCursor.field("joined_rooms")).flatMap(_.as[List[RoomId]]))
    )

  /**
   * Invite a user in the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-invite
   */
  def invite(roomId: RoomId, user: String): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Json](postJson(Seq("rooms", roomId.id, "invite"), Json.Obj("user_id" -> Json.Str(user)))).as(())

  /**
   * Forget the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-forget
   */
  def forget(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Json](
      post(
        Seq("rooms", roomId.id, "forget")
      )
    ).as(())

  /**
   * Leave the given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-leave
   */
  def leave(roomId: RoomId): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Json](
      post(
        Seq("rooms", roomId.id, "leave")
      )
    ).as(())

  /**
   * Ban the user from a  given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-ban
   */
  def ban(roomId: RoomId, user: String, reason: Option[String] = None): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Json](
      postJson(
        Seq("rooms", roomId.id, "ban"),
        Json.Obj(
          "user_id" -> Json.Str(user),
          "reason"  -> Json.Str(reason.getOrElse(""))
        )
      )
    ).as(())

  /**
   * Uban the user from a given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-unban
   */
  def unban(roomId: RoomId, user: String): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Json](
      postJson(
        Seq("rooms", roomId.id, "unban"),
        Json.Obj(
          "user_id" -> Json.Str(user)
        )
      )
    ).as(())

  /**
   * Kick the user from a  given room
   * Documentation: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-kick
   */
  def kick(roomId: RoomId, user: String, reason: Option[String] = None): ZIO[AuthMatrixEnv, MatrixError, Unit] =
    sendWithAuth[Json](
      postJson(
        Seq("rooms", roomId.id, "kick"),
        Json.Obj(
          "user_id" -> Json.Str(user),
          "reason"  -> Json.Str(reason.getOrElse(""))
        )
      )
    ).as(())

}

trait RoomMembershipAccessors {
  def join(roomId: RoomId)                 = ZIO.serviceWithZIO[Matrix](_.join(roomId))
  def joinedRooms                          = ZIO.serviceWithZIO[Matrix](_.joinedRooms)
  def invite(roomId: RoomId, user: String) = ZIO.serviceWithZIO[Matrix](_.invite(roomId, user))
  def forget(roomId: RoomId)               = ZIO.serviceWithZIO[Matrix](_.forget(roomId))
  def leave(roomId: RoomId)                = ZIO.serviceWithZIO[Matrix](_.leave(roomId))

  def ban(roomId: RoomId, user: String, reason: Option[String] = None) =
    ZIO.serviceWithZIO[Matrix](_.ban(roomId, user, reason))
  def unban(roomId: RoomId, user: String) =
    ZIO.serviceWithZIO[Matrix](_.unban(roomId, user))
  def kick(roomId: RoomId, user: String, reason: Option[String] = None) =
    ZIO.serviceWithZIO[Matrix](_.ban(roomId, user, reason))
}

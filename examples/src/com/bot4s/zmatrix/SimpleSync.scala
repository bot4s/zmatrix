package com.bot4s.zmatrix

import zio._
import com.bot4s.zmatrix.api.{ roomMembership, rooms, sync }
import com.bot4s.zmatrix.MatrixError._
import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.models.InviteEvent._
import com.bot4s.zmatrix.models.RoomEvent._
import com.bot4s.zmatrix.services.Authentication
import com.bot4s.zmatrix.client.MatrixClient

import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend

object SimpleSync extends ExampleApp {

  override def runExample(args: List[String]): URIO[AuthMatrixEnv, ExitCode] = {
    lazy val method: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
      sync.sync.tapInviteEvent {
        case (roomId, invite: InviteMemberEvent) if invite.content.membership == "invite" =>
          ZIO.logInfo(f"Joining $roomId") *> roomMembership.join(roomId)
      }.tapRoomEvent { case (roomId, x: RoomMessageText) =>
        ZIO.logInfo(f"${roomId} got: ${x.content.body}") *> ZIO.when(!x.sender.contains("ziobot"))(
          rooms.sendMsg(roomId, EventType.roomMessages, "welcome back")
        )
      }.updateState()

    val mainLoop = method
      .repeat(Schedule.spaced(10.seconds))

    mainLoop.catchSome { case ResponseError("M_MISSING_TOKEN", _, _) | ResponseError("M_UNKNOWN_TOKEN", _, _) =>
      for {
        _ <- ZIO.logError("Invalid or empty token provided, trying password authentication")
        // We want to retry authentication only in case of network error, any other error should terminate the fiber instead
        _ <- Authentication.refresh.refineOrDie { case x: NetworkError => x }
               .retry(Schedule.exponential(1.seconds))
               .tap(token => ZIO.logInfo(token.token))
        program <- mainLoop
      } yield program
    }
      .tapError(error => ZIO.logError(error.toString()))
      .retry(Schedule.forever)
      .exitCode
  }

}

package com.bot4s.zmatrix

import com.bot4s.zmatrix.api.{ roomMembership, rooms, sync }
import com.bot4s.zmatrix.MatrixError._
import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.models.InviteEvent._
import com.bot4s.zmatrix.models.RoomEvent._
import com.bot4s.zmatrix.services.{ Authentication, Logger }
import com.bot4s.zmatrix.client.MatrixClient
import zio.{ ExitCode, Schedule, URIO, ZEnv, ZIO }

import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.magic._
import zio.duration._
import zio.logging.log

object SimpleSync extends ExampleApp {

  override def runExample(args: List[String]): URIO[AuthMatrixEnv, ExitCode] = {
    lazy val method: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
      sync.sync.tapInviteEvent {
        case (roomId, invite: InviteMemberEvent) if invite.content.membership == "invite" =>
          log.info(f"Joining $roomId") *> roomMembership.join(roomId)
      }.tapRoomEvent { case (roomId, x: RoomMessageText) =>
        log.info(f"${roomId} got: ${x.content.body}") *> ZIO.when(!x.sender.contains("ziobot"))(
          rooms.sendMsg(roomId, EventType.roomMessages, "welcome back")
        )
      }.updateState()

    val mainLoop = method
      .repeat(Schedule.spaced(10.seconds))

    mainLoop.catchSome { case ResponseError("M_MISSING_TOKEN", _, _) | ResponseError("M_UNKNOWN_TOKEN", _, _) =>
      for {
        _ <- log.error("Invalid or empty token provided, trying password authentication")
        // We want to retry authentication only in case of network error, any other error should terminate the fiber instead
        _ <- Authentication.refresh.refineOrDie { case x: NetworkError => x }
               .retry(Schedule.exponential(1.seconds))
               .tap(token => log.info(token.token))
        x <- mainLoop
      } yield x
    }
      .tapError(error => log.error(error.toString()))
      .retry(Schedule.forever)
      .exitCode
  }

}

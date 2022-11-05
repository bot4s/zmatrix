package com.bot4s

import zio._

import com.bot4s.zmatrix.MatrixError._
import com.bot4s.zmatrix.client.MatrixClient
import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.models.responses.SyncState
import com.bot4s.zmatrix.services.Authentication
import sttp.client3._

package object zmatrix {
  type MatrixEnv     = MatrixClient with MatrixConfiguration with SyncTokenConfiguration with SttpBackend[Task, Any]
  type AuthMatrixEnv = MatrixEnv with Authentication

  // I'm not sure about those extension method yet, they provide a pretty nice way of writing bots
  implicit class ExtendedZIOErrorState[R, A](syncState: ZIO[R, MatrixError, A]) {
    def withAutoRefresh = syncState.catchSome {
      case ResponseError("M_MISSING_TOKEN", _, _) | ResponseError("M_UNKNOWN_TOKEN", _, _) =>
        for {
          _ <- ZIO.logError("Invalid or empty token provided, trying password authentication")
          // We want to retry authentication only in case of network error, any other error should terminate the fiber instead
          _ <- Authentication.refresh.refineOrDie { case x: NetworkError => x }
                 .tapError(x => ZIO.logError(x.toString()))
                 .retry(Schedule.exponential(1.seconds))
          program <- syncState
        } yield program
    }
  }

  implicit class ExtendedZIOState[R, E](state: ZIO[R, E, SyncState]) {

    def updateState[R1 <: R with SyncTokenConfiguration, E1 >: E]()
      : ZIO[R1 with SyncTokenConfiguration, E1, SyncState] = state.tap[R1, E1] { syncState =>
      SyncTokenConfiguration.get.flatMap { config =>
        SyncTokenConfiguration.set(config.copy(since = Some(syncState.nextBatch)))
      }
    }

    def tapRoomEvent[R1 <: R, E1 >: E](
      pf: PartialFunction[(RoomId, RoomEvent), ZIO[R1, E1, Any]]
    ): ZIO[R1, E1, SyncState] = state.tap[R1, E1] { syncState =>
      val result = for {
        rooms                 <- syncState.rooms.toList
        join                  <- rooms.join.toList
        (roomId, eventsGroup) <- join
        events                 = eventsGroup.timeline.events
      } yield {
        events.collect { case event if pf.isDefinedAt(roomId, event) => pf(roomId, event) }
      }
      ZIO.collectAll(result.flatten)
    }

    def tapInviteEvent[R1 <: R, E1 >: E](
      pf: PartialFunction[(RoomId, InviteEvent), ZIO[R1, E1, Any]]
    ): ZIO[R1, E1, SyncState] = state.tap[R1, E1] { syncState =>
      val result = for {
        rooms                 <- syncState.rooms.toList
        invite                <- rooms.invite.toList
        (roomId, eventsGroup) <- invite
        events                 = eventsGroup.inviteState.events
      } yield {
        events.collect { case event if pf.isDefinedAt(roomId, event) => pf(roomId, event) }
      }
      ZIO.collectAll(result.flatten)
    }
  }
}

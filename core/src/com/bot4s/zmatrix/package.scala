package com.bot4s

import com.bot4s.zmatrix.client.{ MatrixClient, MatrixRequests }
import com.bot4s.zmatrix.models._
import zio.ZIO
import com.bot4s.zmatrix.services.Authentication

package object zmatrix extends MatrixRequests {
  type MatrixEnv     = MatrixClient with MatrixConfiguration with SyncTokenConfiguration
  type AuthMatrixEnv = MatrixEnv with Authentication

  implicit class ExtendedZIOState[R, E](x: ZIO[R, E, SyncState]) {

    def updateState[R1 <: R with SyncTokenConfiguration, E1 >: E]()
      : ZIO[R1 with SyncTokenConfiguration, E1, SyncState] = x.tap[R1, E1] { syncState =>
      SyncTokenConfiguration.get.flatMap { config =>
        SyncTokenConfiguration.set(config.copy(since = Some(syncState.nextBatch)))
      }
    }

    def tapRoomEvent[R1 <: R, E1 >: E](
      pf: PartialFunction[(RoomId, RoomEvent), ZIO[R1, E1, Any]]
    ): ZIO[R1, E1, SyncState] = x.tap[R1, E1] { syncState =>
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
    ): ZIO[R1, E1, SyncState] = x.tap[R1, E1] { syncState =>
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

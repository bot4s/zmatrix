package com.bot4s.zmatrix

import zio._
import zio.stream._

import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.models.responses.SyncState

package object stream {

  type AssociatedEvent[T]  = (RoomId, T)
  type AssociatedEvents[T] = (RoomId, List[T])

  private def flatten[In] = ZPipeline.mapChunks[Chunk[In], In](_.flatten)

  private def saveState = ZPipeline.mapZIO { (syncState: SyncState) =>
    SyncTokenConfiguration.get.flatMap { config =>
      SyncTokenConfiguration.set(config.copy(since = Some(syncState.nextBatch)))
    }.as(syncState)
  }

  val syncedSource =
    ZStream
      .fromZIO(Matrix.sync.withAutoRefresh)
      .via(saveState)

  val inviteEvents = ZPipeline.map { (state: SyncState) =>
    val res = for {
      rooms                 <- state.rooms.toList
      invite                <- rooms.invite.toList
      (roomId, eventsGroup) <- invite
      events                 = eventsGroup.inviteState.events
    } yield (roomId, events)
    Chunk.fromIterable(res)
  } >>> flatten[AssociatedEvents[InviteEvent]]

  val roomEvents = ZPipeline.map { (state: SyncState) =>
    val res = for {
      rooms                 <- state.rooms.toList
      join                  <- rooms.join.toList
      (roomId, eventsGroup) <- join
      events                 = eventsGroup.timeline.events
    } yield (roomId, events)
    Chunk.fromIterable(res)
  } >>> flatten[AssociatedEvents[RoomEvent]]

  val messageEvents = roomEvents >>>
    ZPipeline.map[AssociatedEvents[RoomEvent], AssociatedEvents[MessageEvent]] { case (roomId, events) =>
      (roomId, events.collect { case msg: MessageEvent => msg })
    }

  private[stream] def onEvent[T](
    pf: PartialFunction[(RoomId, T), Any]
  ): ZPipeline[Any, Nothing, AssociatedEvents[T], AssociatedEvents[T]] =
    ZPipeline.map { (input: AssociatedEvents[T]) =>
      val (roomId, events) = input

      val _ = events.collect {
        case event if pf.isDefinedAt(roomId, event) => pf(roomId, event)
      }

      input
    }

  private[stream] def onEventZIO[R, E, T](
    pf: PartialFunction[AssociatedEvent[T], ZIO[R, E, Any]]
  ) =
    ZPipeline.mapZIO { (input: AssociatedEvents[T]) =>
      val (roomId, events) = input
      val res = events.collect {
        case event if pf.isDefinedAt(roomId, event) => pf(roomId, event)
      }
      ZIO.collectAll(res).as((roomId, events))
    }

  // issue: what is we want to filter the messages between the roomEvens and the onEvent
  def onRoomEvent(pf: PartialFunction[AssociatedEvent[RoomEvent], Any]) =
    roomEvents >>> onEvent(pf)

  def onRoomEventZIO[R, E](pf: PartialFunction[AssociatedEvent[RoomEvent], ZIO[R, E, Any]]) =
    roomEvents >>> onEventZIO(pf)

  def onMessageEvent(pf: PartialFunction[AssociatedEvent[MessageEvent], Any]) =
    onEvent(pf)

  def onMessageEventZIO[R, E](pf: PartialFunction[AssociatedEvent[MessageEvent], ZIO[R, E, Any]]) =
    onEventZIO(pf)

  def onInviteEvent(pf: PartialFunction[AssociatedEvent[InviteEvent], Any]) =
    inviteEvents >>> onEvent(pf)

  def onInviteEventZIO[R, E](pf: PartialFunction[AssociatedEvent[InviteEvent], ZIO[R, E, Any]]) =
    inviteEvents >>> onEventZIO(pf)

  val filterBotMessage = {
    val fromBot = (config: MatrixConfiguration, sender: String) =>
      config.matrix.userId.exists(name => sender.startsWith(s"@$name"))
    ZPipeline.mapZIO[AuthMatrixEnv, MatrixError, AssociatedEvents[MessageEvent], AssociatedEvents[MessageEvent]] {
      case (roomId, events) =>
        for {
          config <- ZIO.service[MatrixConfiguration]
        } yield (roomId, events.filter(event => !fromBot(config, event.sender)))
    }
  }

  def addCallbacks[R, E](source: ZStream[R, E, SyncState], cb: List[ZPipeline[R, E, SyncState, Any]]): ZIO[R, E, Unit] =
    source
      .broadcast(cb.size, 5)
      .flatMap { streams =>
        ZStream
          .mergeAllUnbounded(16)(
            streams.zip(Chunk.fromIterable(cb)).map { case (stream, cb) => stream.via(cb) }: _*
          )
          .runDrain
      }
      .provideSome[R](Scope.default)
}

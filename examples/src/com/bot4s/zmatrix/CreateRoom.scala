package com.bot4s.zmatrix

import zio.Console._
import zio._

import com.bot4s.zmatrix.api.{ roomCreation, rooms }
import com.bot4s.zmatrix.models._

object CreateRoom extends ExampleApp[ExitCode] {

  override def runExample: URIO[AuthMatrixEnv, ExitCode] =
    roomCreation
      .createRoom(
        RoomCreationData(
          name = Some("Some Test Room"),
          roomAliasName = Some("TestRoomAlias"),
          preset = Some(Preset.privateChat),
          topic = Some("A ZIO Bot created this room"),
          // invite = Some(List("@exampleUser:matrix.org")),
          visibility = Some(Visibility.privateRoom)
        )
      )
      .flatMap { roomId =>
        ZIO.logInfo(s"Created room with id $roomId") *>
          // roomMembership.invite(roomId, "@exampleUser:matrix.org") *>
          rooms.sendMsg(roomId, "Welcome to my room")
      }
      .tapError(e => printLineError(e.toString()))
      .flatMap(x => printLine(x.toString()))
      .exitCode

}

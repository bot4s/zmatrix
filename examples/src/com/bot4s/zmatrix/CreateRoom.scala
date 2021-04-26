package com.bot4s.zmatrix

import com.bot4s.zmatrix.api.{ accounts, roomCreation, roomMembership, rooms }
import zio.console._
import zio.{ ExitCode, URIO }
import com.bot4s.zmatrix.models.RoomCreationData
import com.bot4s.zmatrix.models.Preset
import com.bot4s.zmatrix.models.Visibility
import com.bot4s.zmatrix.models.RoomId
import com.bot4s.zmatrix.models.EventType
import zio.logging.log
import zio._

object CreateRoom extends ExampleApp {

  override def runExample(args: List[String]): URIO[AuthMatrixEnv, ExitCode] =
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
        log.info(s"Created room with id $roomId") *>
          // roomMembership.invite(roomId, "@exampleUser:matrix.org") *>
          rooms.sendMsg(roomId, EventType.roomMessages, "Welcome to my room")
      }
      .tapError(e => putStrLn(e.toString()))
      .flatMap(x => putStrLn(x.toString()))
      .exitCode

}

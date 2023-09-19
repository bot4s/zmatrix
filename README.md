# zmatrix - ZIO Matrix

A [matrix](https://matrix.org/) client implemented using ZIO.

| CI              | Release                                                               | Snapshot                                                                 |
| --------------- | --------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| ![CI][badge-ci] | [![Release Artifacts][badge-sonatypereleases]][link-sonatypereleases] | [![Snapshot Artifacts][badge-sonatypesnapshots]][link-sonatypesnapshots] |

## Installation

Add the following dependency to your project's build file

```scala
// sbt
"com.bot4s" %% "zmatrix" % "0.3.3"
// mill
ivy"com.bot4s::zmatrix:0.3.3"
```

It is also possible to get the latest snapshot from [Snapshot Artifacts][link-sonatypesnapshots] by adding the following
lines to your `build.sc`:

```scala
def repositories = super.repositories ++ Seq(
  MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
)
```

And then `ivy"com.bot4s::zmatrix:$SNAPSHOT-VERSION"`

## Description

zmatrix is a library designed to communicate with a Matrix API.

Matrix' API is huge and cover a lot of different topics, only a few endpoints are currently implemented.
The list of endpoints is defined here: https://matrix.org/docs/api/client-server/.

If there is one missing, or you find a bug in how it is implemented please submit an issue or PR.

Most implementation will be similar to existing endpoint, but automatic derivation or codegen from the OpenAPI schema is not an option right now (the first result were awful and unreadable).

## Usage

The main usage for this library is to write bot and automate task using a Matrix server. The current design is using a pull based approach to retrieve
all the events addressed to the client.

It is then possible to attach some hooks to respond to changes from events.

In order to receive messages from a room, the bot must accept the invitation. This can be done using the following code:

```scala
import com.bot4s.zmatrix.api.{ roomMembership, sync }
// sync is to retrieve the state, roomMembership will contains all the endpoints related to room and user management (such as invites)


lazy val acceptAllInvite: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
  sync.sync.tapInviteEvent {
    case (roomId, invite: InviteMemberEvent) if invite.content.membership == "invite" =>
      log.info(f"Joining $roomId") *> roomMembership.join(roomId)
}
```

This snippet of code attaches a callback on the retrieve state (`sync.sync`) and will accept any invitation.

Now that we are allowed to send messages to a room, let's write an echo bot to demonstrate the usage of the room events hook:

```scala
import com.bot4s.zmatrix.api.{ roomMembership, rooms, sync }
// the rooms import is required to send a message

val mirrorEcho : ZIO[AuthMatrixEnv, MatrixError, SyncState] = acceptAllInvite.tapRoomEvent { case (roomId, x: RoomMessageText) =>
  log.info(f"${roomId} got: ${x.content.body}") *> ZIO.when(!x.sender.contains("ziobot"))(
    rooms.sendMsg(roomId, EventType.roomMessages, x.content.body.reverse)
  )
}
```

This will attach a hook to the state that enable us to inspect the messages received. Note that we need to filter the message that we are sending
because the state sync will forward them back to use, creating an infinite loop of messages.

We can now attach a scheduler to this effect to pull our state at fixed intervals

```scala
val mainLoop = mirrorEcho
  .updateState()
  .repeat(Schedule.spaced(10.seconds))
```

The `updateState` method is a key piece here. Matrix API rely heavily on pagination to keep track of the state, it will by default return the whole state
for a client. By calling `updateState` we make sure to save our current pagination and only ask for newer events to the API.

The last step is to provide and environment to this effect, we are using the amazing [zio-magic](https://github.com/kitlangton/zio-magic) lib to help us with
the `ZLayer` configuration:

```scala
val loggingLayer = Logging.console(
  logLevel = LogLevel.Info,
  format = LogFormat.ColoredLogFormat()
) >>> Logging.withRootLoggerName("matrix-zio-sync")

mainLoop.inject(
  ZEnv.live,
  loggingLayer,
  MatrixConfiguration.persistent(), // will read/write the configuration from a `bot.conf` file in the project's resources
  Authentication.live, // A HTTP middleware that will take care of creating/checking validity of credential/token
  AsyncHttpClientZioBackend.layer(), // STTP zio backend to perform HTTP queries
  MatrixClient.live // The actual implementation of the client
)
.retry(Schedule.forever)
.exitCode
```

## Examples

The `examples` package comtains runnable examples for `zmatrix`. Examples can be selected by changing the `mainClass` definition in the `build.sc` or by running the following command:

```
mill -i examples[_].run
```

## Building

A local version of the library can be published by running those two commands:

```
mill __.publishM2Local
mill __.publishLocal
```

## References

The design of the lib was heavily inspired by the work done on [zio-slack](https://github.com/Dapperware/zio-slack/blob/master/README.md).

The actual endpoint implementation and reference for the Matrix API is taken from [matrix-nio](https://github.com/poljar/matrix-nio/) project, written in python.

[link-sonatypereleases]: https://oss.sonatype.org/content/repositories/releases/com/bot4s/zmatrix_2.13/ "Sonatype Releases"
[link-sonatypesnapshots]: https://oss.sonatype.org/content/repositories/snapshots/com/bot4s/zmatrix_2.13/ "Sonatype Snapshots"
[badge-ci]: https://github.com/bot4s/zmatrix/workflows/Build/badge.svg
[badge-sonatypereleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/com.bot4s/zmatrix_2.13.svg "Sonatype Releases"
[badge-sonatypesnapshots]: https://img.shields.io/nexus/s/https/oss.sonatype.org/com.bot4s/zmatrix_2.13.svg "Sonatype Snapshots"

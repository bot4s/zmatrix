package com.bot4s.zmatrix

import zio.test.Assertion._
import zio.test._

import com.bot4s.zmatrix.models.RoomMessageType._
import com.bot4s.zmatrix.models.{MessageEvent, Preset, RoomCreationData, RoomEvent, RoomMessageType}
import io.circe.Json
import io.circe.parser.decode
import io.circe.syntax._

object SerializationSpec extends ZIOSpecDefault {

  def spec = suite("Serialization")(
    test("Room Creation") {
      val json = RoomCreationData(
        preset = Some(Preset.publicChat),
        roomAliasName = Some("testroomalias"),
        name = Some("testroom"),
        topic = Some("Custom topic")
      ).asJson.dropNullValues

      assert(json)(
        equalTo(
          Json.obj(
            "preset"          -> Json.fromString("public_chat"),
            "room_alias_name" -> Json.fromString("testroomalias"),
            "name"            -> Json.fromString("testroom"),
            "topic"           -> Json.fromString("Custom topic")
          )
        )
      )
    },
    test("RoomMessageType encoder") {
      val obj = (RoomMessageTextContent("test"): RoomMessageType).asJson.deepDropNullValues
      assert(obj.deepDropNullValues)(
        equalTo(Json.obj("body" -> Json.fromString("test"), "msgtype" -> Json.fromString("m.text")))
      )
    },
    test("RoomMessageTextContent encoder") {
      val obj = RoomMessageTextContent("test").asJson.deepDropNullValues
      assert(obj)(equalTo(Json.obj("body" -> Json.fromString("test"), "msgtype" -> Json.fromString("m.text"))))
    },
    test("RoomMessageType decoder") {
      val res = decode[RoomMessageType]("""{ "body": "test", "msgtype": "m.text" }""")
      assert(res.toOption)(isSome(equalTo(RoomMessageTextContent("test"))))
    },
    test("m.room.message") {
      val source = """
      {
  "type": "m.room.message",
  "sender": "@bot:matrix.org",
  "content": {
    "msgtype": "m.text",
    "body": "success"
  },
  "origin_server_ts": 1666766141000,
  "unsigned": {
    "age": 75
  },
  "event_id": "$5w9tsY4TSSgW_sTVeyt1MlpgW0N_XuTvNntK111-JmI",
  "room_id": "!my_roomt:matrix.org"
}
      """
      val res    = decode[RoomEvent](source)
      assert(res.toOption)(
        isSome(
          equalTo(
            MessageEvent(
              sender = "@bot:matrix.org",
              eventId = "$5w9tsY4TSSgW_sTVeyt1MlpgW0N_XuTvNntK111-JmI",
              content = RoomMessageTextContent("success")
            )
          )
        )
      )
    }
  )
}

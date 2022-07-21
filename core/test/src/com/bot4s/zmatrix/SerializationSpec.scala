package com.bot4s.zmatrix

import zio.test._
import io.circe.syntax._
import zio.test.Assertion._
import io.circe.Json
import com.bot4s.zmatrix.models.{ Preset, RoomCreationData }

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
    }
  )
}

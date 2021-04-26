package com.bot4s.zmatrix

import zio.test._
import io.circe.syntax._
import zio.test.Assertion._
import zio.test.DefaultRunnableSpec
import com.bot4s.zmatrix.models.{ Preset, RoomCreationData }
import io.circe.Json

object SerializationSpec extends DefaultRunnableSpec {
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

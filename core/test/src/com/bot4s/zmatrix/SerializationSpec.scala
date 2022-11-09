package com.bot4s.zmatrix

import zio.json._
import zio.json.ast._
import zio.test.Assertion._
import zio.test._

import com.bot4s.zmatrix.models.RoomEvent._
import com.bot4s.zmatrix.models.RoomMessageType._
import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.models.responses.SyncState

object SerializationSpec extends ZIOSpecDefault {

  def spec = suite("Serialization")(
    suite("encoders")(
      test("Room Creation") {
        val json = RoomCreationData(
          preset = Some(Preset.publicChat),
          roomAliasName = Some("testroomalias"),
          name = Some("testroom"),
          topic = Some("Custom topic")
        ).toJsonAST

        assert(json)(
          isRight(
            equalTo(
              Json.Obj(
                "preset"          -> Json.Str("public_chat"),
                "room_alias_name" -> Json.Str("testroomalias"),
                "name"            -> Json.Str("testroom"),
                "topic"           -> Json.Str("Custom topic")
              )
            )
          )
        )
      },
      test("RoomMessageTextContent as RoomMessageType") {
        val obj = (RoomMessageTextContent("test"): RoomMessageType).toJsonAST
        assert(obj)(
          isRight(equalTo(Json.Obj("body" -> Json.Str("test"), "msgtype" -> Json.Str("m.text"))))
        )
      },
      test("RoomMessageTextContent") {
        val obj = RoomMessageTextContent("test").toJsonAST
        assert(obj)(isRight(equalTo(Json.Obj("body" -> Json.Str("test"), "msgtype" -> Json.Str("m.text")))))
      },
      test("RoomEmptyMessage") {
        val obj = RoomMessageEmpty.toJsonAST
        assert(obj)(isRight(equalTo(Json.Obj())))
      }
    ),
    suite("decoders")(
      test("RoomMessageType decoder") {
        val res = """{ "body": "test", "msgtype": "m.text" }""".fromJson[RoomMessageType]
        assert(res)(isRight(equalTo(RoomMessageTextContent("test"))))
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
        val res    = source.fromJson[RoomEvent]
        assert(res)(
          isRight(
            equalTo(
              MessageEvent(
                sender = "@bot:matrix.org",
                eventId = "$5w9tsY4TSSgW_sTVeyt1MlpgW0N_XuTvNntK111-JmI",
                content = RoomMessageTextContent("success")
              )
            )
          )
        )
      },
      test("redacted") {
        val source = """
{
  "type": "m.room.message",
  "sender": "@user:matrix.org",
  "content": {},
  "origin_server_ts": 1666874589140,
  "unsigned": {
      "redacted_by": "$PTBHIj9ZCpLTYBUdzsBumOnA-ozXfmhssdwY-5dMYUg",
      "redacted_because": {
          "type": "m.room.redaction",
          "sender": "@other:matrix.org",
          "content": {},
          "redacts": "$gqlw0nXSUGTqfgsFMvdHJagjPkcuUy7cTG79sjGcVqc",
          "origin_server_ts": 1666874601850,
          "unsigned": {
              "age": 111624586
          },
          "event_id": "$PTBHIj9ZCpLTYBUdzsBumOnA-ozXfmhssdwY-5dMYUg"
      },
      "age": 111637296
  },
  "event_id": "$gqlw0nXSUGTqfgsFMvdHJagjPkcuUy7cTG79sjGcVqc"
}
      """
        val res    = source.fromJson[RoomEvent]
        assert(res)(
          isRight(
            equalTo(
              MessageEvent(
                sender = "@user:matrix.org",
                eventId = "$gqlw0nXSUGTqfgsFMvdHJagjPkcuUy7cTG79sjGcVqc",
                content = RoomMessageEmpty
              )
            )
          )
        )
      },
      test("RoomId") {
        assert(""""test"""".fromJson[RoomId])(isRight(equalTo(RoomId("test"))))
        assert("""{"id": "test"}""".fromJson[RoomId])(isRight(equalTo(RoomId("test"))))
      },
      test("RoomMessageTextContent") {
        val content = """
        {
            "msgtype": "m.text",
            "body": "success"
        }
        """

        assert(content.fromJson[RoomMessageType])(isRight(equalTo(RoomMessageTextContent("success"))))
        assert(content.fromJson[RoomMessageTextContent])(isRight(equalTo(RoomMessageTextContent("success"))))
      },
      test("RoomMessageTextContent - formatted body") {
        val content = """
        {
            "msgtype": "m.text",
            "body": "success",
            "formatted_body":"success - formatted"
        }
        """

        assert(content.fromJson[RoomMessageType])(
          isRight(equalTo(RoomMessageTextContent("success", formattedBody = Some("success - formatted"))))
        )
        assert(content.fromJson[RoomMessageTextContent])(
          isRight(equalTo(RoomMessageTextContent("success", formattedBody = Some("success - formatted"))))
        )
      },
      test("RoomMessageImageContent") {
        val content = """
        {
            "msgtype": "m.image",
            "body": "success",
            "url": "http://fake.matrixbot/bot.jpg"
        }
        """

        assert(content.fromJson[RoomMessageType])(
          isRight(equalTo(RoomMessageImageContent("success", Some("http://fake.matrixbot/bot.jpg"))))
        )
        assert(content.fromJson[RoomMessageImageContent])(
          isRight(equalTo(RoomMessageImageContent("success", Some("http://fake.matrixbot/bot.jpg"))))
        )
      },
      test("RoomMessageTextContent encode") {
        val result = """
        {
            "body": "success",
            "msgtype": "m.text"
        }
        """.replaceAll("\\s", "")
        assert(RoomMessageTextContent("success").toJson)(equalTo(result))
      },
      test("decode") {
        val content = """
        {
            "next_batch": "s735798_20141841_27974_715432_326180_115_59046_739295_0",
            "presence": {
                "events": [
                    {
                        "type": "m.presence",
                        "sender": "@ex0ns:matrix.org",
                        "content": {
                            "presence": "online",
                            "last_active_ago": 215,
                            "currently_active": true
                        }
                    },
                    {
                        "type": "m.presence",
                        "sender": "@ziobot:matrix.org",
                        "content": {
                            "presence": "online",
                            "last_active_ago": 28,
                            "currently_active": true
                        }
                    }
                ]
            },
            "device_lists": {
                "changed": [
                    "@ziobot:matrix.org"
                ]
            },
            "device_one_time_keys_count": {
                "signed_curve25519": 0
            },
            "org.matrix.msc2732.device_unused_fallback_key_types": [],
            "device_unused_fallback_key_types": [],
            "rooms": {
                "join": {
                    "!sdUfnyuUPYtPGbcZhj:matrix.org": {
                        "timeline": {
                            "events": [
                                {
                                    "type": "m.room.message",
                                    "sender": "@ziobot:matrix.org",
                                    "content": {
                                        "body": "CC Logo",
                                        "url": "mxc://matrix.org/IkaslHFPELmKVhXDDGMCKUWt",
                                        "info": {
                                            "h": 100,
                                            "w": 100
                                        },
                                        "msgtype": "m.image"
                                    },
                                    "origin_server_ts": 1667982642143,
                                    "unsigned": {
                                        "age": 390843
                                    },
                                    "event_id": "$sgL3nknhUljN5T7bqCYS5FC8pYK3GBe_o7JXC94NDfI"
                                }
                            ],
                            "prev_batch": "s735790_20141841_27974_715432_326180_115_59046_739295_0",
                            "limited": false
                        },
                        "state": {
                            "events": []
                        },
                        "account_data": {
                            "events": []
                        },
                        "ephemeral": {
                            "events": [
                                {
                                    "type": "m.receipt",
                                    "content": {
                                        "$sgL3nknhUljN5T7bqCYS5FC8pYK3GBe_o7JXC94NDfI": {
                                            "m.read": {
                                                "@ex0ns:matrix.org": {
                                                    "ts": 1667982687776
                                                }
                                            }
                                        }
                                    }
                                }
                            ]
                        },
                        "unread_notifications": {
                            "notification_count": 29,
                            "highlight_count": 0
                        },
                        "summary": {}
                    }
                }
            }
        }
      
      """

        assert(content.fromJson[SyncState])(
          isRight(
            equalTo(
              SyncState(
                None,
                "s735798_20141841_27974_715432_326180_115_59046_739295_0",
                Some(
                  Rooms(
                    None,
                    Some(
                      Map(
                        RoomId("!sdUfnyuUPYtPGbcZhj:matrix.org") -> JoinedRoom(
                          RoomEventTimeline(
                            List(
                              MessageEvent(
                                "@ziobot:matrix.org",
                                "$sgL3nknhUljN5T7bqCYS5FC8pYK3GBe_o7JXC94NDfI",
                                RoomMessageImageContent(
                                  "CC Logo",
                                  Some("mxc://matrix.org/IkaslHFPELmKVhXDDGMCKUWt"),
                                  Some(
                                    ImageInfo(
                                      w = Some(100),
                                      h = Some(100)
                                    )
                                  )
                                )
                              )
                            ),
                            false
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        )

      }
    )
  )
}

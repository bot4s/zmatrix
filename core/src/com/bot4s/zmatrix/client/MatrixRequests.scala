package com.bot4s.zmatrix.client

import zio.{ URIO, ZIO }

import com.bot4s.zmatrix._
import com.bot4s.zmatrix.core.{ ApiScope, JsonRequest, MatrixBody }
import io.circe.Json
import sttp.model.{ MediaType, Method }

/**
 * This trait provides all the helper methods related to the queries that must
 * be send to the Matrix server. It must be used to serialize requests' body and
 * query parameters.
 */
trait MatrixRequests {

  def get(path: Seq[String]) =
    JsonRequest(Method.GET, path)

  def postJson(path: Seq[String], body: Json) =
    sendJson(Method.POST, path, body)

  def putJson(path: Seq[String], body: Json) =
    sendJson(Method.PUT, path, body)

  def post(path: Seq[String]) =
    postJson(path, Json.obj())

  /*
    This method is a bit specific as it is only for `media` file
    this might need a refactor in the future, but as of now,
    it can not be used to send another file
   */
  def uploadMediaFile(content: Array[Byte], contentType: MediaType): JsonRequest =
    JsonRequest(Method.POST, Seq("upload"), MatrixBody.ByteBody(content, contentType)).withScope(ApiScope.Media)

  def withSince(request: JsonRequest): URIO[AuthMatrixEnv, JsonRequest] =
    ZIO.serviceWithZIO[SyncTokenConfiguration] { config =>
      config.get.map { config =>
        request.addParam("since" -> config.since)
      }
    }

  private def sendJson(method: Method, path: Seq[String], body: Json) =
    JsonRequest(method, path, MatrixBody.JsonBody(body.deepDropNullValues))
}

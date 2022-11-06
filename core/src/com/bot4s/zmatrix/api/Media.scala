package com.bot4s.zmatrix.api

import zio._

import java.io.File
import java.nio.file.Files
import scala.util.Try

import com.bot4s.zmatrix.MatrixError._
import com.bot4s.zmatrix.models.MxcUri
import com.bot4s.zmatrix.{ AuthMatrixEnv, Matrix, MatrixApiBase, MatrixError }
import sttp.client3._
import sttp.model.{ MediaType, Method, Uri }

trait Media { self: MatrixApiBase =>

  def upload(file: File, contentType: Option[MediaType]): ZIO[AuthMatrixEnv, MatrixError, MxcUri] = {
    def getContentType = Try(Files.probeContentType(file.toPath())).toOption.flatMap(ct => MediaType.parse(ct).toOption)

    val ct = contentType.orElse(getContentType).getOrElse(MediaType.ApplicationOctetStream)

    for {
      content <- ZIO
                   .attemptBlocking(Files.readAllBytes(file.toPath()))
                   .mapError(t => ClientError(s"Unable to read ${file.toPath().toString()} content for upload", t))
      result <- sendWithAuth[MxcUri](uploadMediaFile(content, ct))
    } yield result
  }

  /*
  This is a small helper that can get the content of a remote file and upload it on the
  matrix repository.
   */
  def upload(
    url: Uri,
    contentType: Option[MediaType]
  ): ZIO[AuthMatrixEnv with SttpBackend[Task, Any], MatrixError, MxcUri] = {
    val response = ZIO.serviceWithZIO[SttpBackend[Task, Any]] { backend =>
      basicRequest
        .method(Method.GET, url)
        .response(asByteArrayAlways)
        .mapResponse(Right[MatrixError, Array[Byte]](_))
        .send(backend)
        .mapError(t => NetworkError(f"Unable to fetch resource at ${url}", t))
    }

    val body = response.flatMap(response => ZIO.fromEither(response.body))
    val requestContentType =
      response.flatMap(resp => ZIO.fromOption(resp.header("content-type").flatMap(ct => MediaType.parse(ct).toOption)))

    val ct = ZIO
      .from(contentType)
      .orElse(requestContentType)
      .orElseSucceed(MediaType.ImageJpeg)

    for {
      content     <- body
      contentType <- ct
      result      <- sendWithAuth[MxcUri](uploadMediaFile(content, contentType))
    } yield result
  }
}

private[zmatrix] trait MediaAccessors {
  def upload(file: File, contentType: Option[MediaType]) =
    ZIO.serviceWithZIO[Matrix](_.upload(file, contentType))
  def upload(url: Uri, contentType: Option[MediaType]) =
    ZIO.serviceWithZIO[Matrix](_.upload(url, contentType))
}

package com.bot4s.zmatrix.api

import zio.ZIO

import java.io.File
import java.nio.file.Files
import scala.util.Try

import com.bot4s.zmatrix.MatrixError._
import com.bot4s.zmatrix.models.MxcUri
import com.bot4s.zmatrix.{ AuthMatrixEnv, MatrixError }
import sttp.client3._
import sttp.model.{MediaType, Method, Uri}

trait Media {

  def upload(file: File, contentType: Option[MediaType]): ZIO[AuthMatrixEnv, MatrixError, MxcUri] = {
    def getContentType = Try(Files.probeContentType(file.toPath())).toOption.flatMap(ct => MediaType.parse(ct).toOption)

    val ct = contentType.orElse(getContentType).getOrElse(MediaType.ApplicationOctetStream)

    for {
      content <- ZIO
                   .attemptBlocking(Files.readAllBytes(file.toPath()))
                   .mapError(t => ClientError(s"Unable to read ${file.toPath().toString()} content for upload", t))
      result <- sendWithAuth[MxcUri](postMediaFile(Seq("upload"), content, ct))
    } yield result
  }

  /*
  This is a small helper that can get the content of a remote file and upload it on the
  matrix repository.
  As of now it's using the MatrixClient because it's already in scope, but it might be better
  to introduce another "external" client that we can use for this kind of things.
  Because of the limitation of the send method of the MatrixClient, we can't access the header here
  this could have been useful to propagate the contentType
   */
  def upload(url: Uri, contentType: MediaType): ZIO[AuthMatrixEnv, MatrixError, MxcUri] = {
    val req: Request[MatrixResponse[Array[Byte]], Any] =
      basicRequest
        .method(Method.GET, url)
        .response(asByteArrayAlways)
        .mapResponse(Right[MatrixResponseError, Array[Byte]](_))

    for {
      content <- send(req)
      result  <- sendWithAuth[MxcUri](postMediaFile(Seq("upload"), content, contentType))
    } yield result
  }
}

object media extends Media

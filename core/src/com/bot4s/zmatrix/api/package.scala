package com.bot4s.zmatrix

import zio.ZIO

import com.bot4s.zmatrix.client.{ MatrixClient, MatrixParser, MatrixRequests }
import io.circe.Decoder
import sttp.client3.Request

package object api extends MatrixRequests with WithAccess with MatrixParser {

  def send[T](
    request: Request[MatrixResponse[T], Any]
  ): ZIO[MatrixClient, MatrixError, T] =
    ZIO.environmentWithZIO[MatrixClient](_.get.send(request))

  def send[T](action: MatrixAction)(implicit decoder: Decoder[T]): ZIO[MatrixEnv, MatrixError, T] =
    for {
      req      <- action
      response <- send(req)
      decoded  <- as(response)(decoder)
    } yield decoded

  def sendWithAuth[T](action: MatrixAction)(implicit decoder: Decoder[T]): ZIO[AuthMatrixEnv, MatrixError, T] =
    for {
      req      <- action
      auth     <- authenticate(req)
      response <- send(auth)
      decoded  <- as(response)(decoder)
    } yield decoded
}

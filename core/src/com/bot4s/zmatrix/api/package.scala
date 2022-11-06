package com.bot4s.zmatrix

import zio.ZIO

import com.bot4s.zmatrix.client.{ MatrixClient, MatrixParser, MatrixRequests }
import com.bot4s.zmatrix.core.JsonRequest
import com.bot4s.zmatrix.core.RequestAuth._
import com.bot4s.zmatrix.services.Authentication
import io.circe.Decoder

package object api extends MatrixRequests with MatrixParser {

  def send[T](request: JsonRequest)(implicit decoder: Decoder[T]): ZIO[MatrixClient, MatrixError, T] =
    for {
      response <- ZIO.serviceWithZIO[MatrixClient](_.send(request))
      decoded  <- as(response)(decoder)
    } yield decoded

  def sendWithAuth[T](request: JsonRequest)(implicit decoder: Decoder[T]): ZIO[AuthMatrixEnv, MatrixError, T] =
    for {
      token    <- Authentication.accessToken
      withAuth  = request.withAuth(TokenAuth(token.token))
      response <- send(withAuth)
    } yield response
}

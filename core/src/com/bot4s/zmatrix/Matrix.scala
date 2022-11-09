package com.bot4s.zmatrix

import zio._
import zio.json.JsonDecoder

import com.bot4s.zmatrix.api._
import com.bot4s.zmatrix.client._
import com.bot4s.zmatrix.core.JsonRequest
import com.bot4s.zmatrix.core.RequestAuth._
import com.bot4s.zmatrix.services.Authentication

trait MatrixApiBase extends MatrixRequests with MatrixParser {
  def client: MatrixClient

  def send[T](request: JsonRequest)(implicit decoder: JsonDecoder[T]): IO[MatrixError, T] =
    for {
      response <- client.send(request)
      decoded  <- as(response)(decoder)
    } yield decoded

  def sendWithAuth[T](request: JsonRequest)(implicit decoder: JsonDecoder[T]): ZIO[AuthMatrixEnv, MatrixError, T] =
    for {
      token    <- Authentication.accessToken
      withAuth  = request.withAuth(TokenAuth(token.token))
      response <- send(withAuth)(decoder)
    } yield response

}

trait Matrix
    extends MatrixApiBase
    with Account
    with DeviceManagement
    with Login
    with Logout
    with Media
    with Rooms
    with RoomCreation
    with RoomMembership
    with Sync

object Matrix
    extends AccountAccessors
    with DeviceManagementAccessors
    with LoginAccessors
    with LogoutAccessors
    with MediaAccessors
    with RoomAccessors
    with RoomCreationAccessors
    with RoomMembershipAccessors
    with SyncAccessors {

  def make: URLayer[MatrixClient, Matrix] = ZLayer {
    ZIO.serviceWith[MatrixClient](c =>
      new Matrix {
        val client = c
      }
    )
  }
}

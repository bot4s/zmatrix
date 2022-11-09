package com.bot4s.zmatrix.api

import zio.ZIO
import zio.json.ast._

import com.bot4s.zmatrix.models.Device
import com.bot4s.zmatrix.{ MatrixError, _ }

trait DeviceManagement { self: MatrixApiBase =>
  /*
   * Get information about all devices for the current user
   * Documentation: https://matrix.org/docs/spec/client_server/latest#get-matrix-client-r0-devices
   */
  def getDevices: ZIO[AuthMatrixEnv, MatrixError, List[Device]] =
    sendWithAuth[List[Device]](get(Seq("devices")))(
      Json.decoder.mapOrFail(_.get(JsonCursor.field("devices")).flatMap(_.as[List[Device]]))
    )
}

private[zmatrix] trait DeviceManagementAccessors {
  def getDevices = ZIO.serviceWithZIO[Matrix](_.getDevices)
}

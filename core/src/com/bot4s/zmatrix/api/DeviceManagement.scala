package com.bot4s.zmatrix.api

import zio.ZIO

import com.bot4s.zmatrix.models.Device
import com.bot4s.zmatrix.{ MatrixError, _ }

trait DeviceManagement {
  /*
   * Get information about all devices for the current user
   * Documentation: https://matrix.org/docs/spec/client_server/latest#get-matrix-client-r0-devices
   */
  def getDevices(): ZIO[AuthMatrixEnv, MatrixError, List[Device]] =
    sendWithAuth(get(Seq("devices")))(_.downField("devices").as[List[Device]])
}

object devices extends DeviceManagement;

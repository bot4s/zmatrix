package com.bot4s.zmatrix

import zio.URIO

import com.bot4s.zmatrix.services.Authentication
import sttp.client3.RequestT

trait WithAccess {

  def authenticate[U[_], T](request: RequestT[U, T, Any]): URIO[Authentication, RequestT[U, T, Any]] =
    Authentication.accessToken.flatMap(_.authenticateM(request))

}

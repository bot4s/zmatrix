package com.bot4s.zmatrix

import zio.{ Has, URIO }
import sttp.client3.RequestT
import com.bot4s.zmatrix.services.Authentication

trait WithAccess {

  def authenticate[U[_], T](request: RequestT[U, T, Any]): URIO[Has[Authentication], RequestT[U, T, Any]] =
    Authentication.accessToken.flatMap(_.authenticateM(request))

}

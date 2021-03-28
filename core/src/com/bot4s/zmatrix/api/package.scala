package com.bot4s.zmatrix

import sttp.client3.Request
import zio.{ Has, ZIO }
import com.bot4s.zmatrix.client.{ MatrixClient, MatrixParser, MatrixRequests }

package object api extends MatrixRequests with WithAccess with MatrixParser {

  def send[T](
    request: Request[MatrixResponse[T], Any]
  ): ZIO[Has[MatrixClient], MatrixError, T] =
    ZIO.accessM[Has[MatrixClient]](_.get.send(request))
}

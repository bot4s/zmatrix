package com.bot4s.zmatrix.core

sealed trait ApiScope

object ApiScope {
  case object Client extends ApiScope
  case object Media  extends ApiScope
}

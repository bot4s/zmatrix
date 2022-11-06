package com.bot4s.zmatrix.core

sealed trait RequestAuth
object RequestAuth {
  case object NoAuth                  extends RequestAuth
  case class TokenAuth(token: String) extends RequestAuth
}

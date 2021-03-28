package com.bot4s.zmatrix.models

object EventType extends Enumeration {
  type EventType = Value
  val roomMessages  = Value("m.room.message")
  val tokenLogin    = Value("m.login.token")
  val passwordLogin = Value("m.login.password")

}

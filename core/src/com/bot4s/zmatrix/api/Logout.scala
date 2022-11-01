package com.bot4s.zmatrix.api

trait Logout {

  def logout =
    sendWithAuth[Unit](post(Seq("logout")))

  def logoutAll =
    sendWithAuth[Unit](post(Seq("logout", "all")))
}

object logout extends Logout

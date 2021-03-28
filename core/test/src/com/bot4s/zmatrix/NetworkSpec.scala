package com.bot4s.zmatrix

import zio.test._
import zio.test.Assertion._
import zio.test.DefaultRunnableSpec

object NetworkSpec extends DefaultRunnableSpec {
  def spec = suite("NetworkSpec")(
    test("Should correctly add two numbers") {
      assert(1 + 1)(equalTo(2))
    }
  )
}

package com.bot4s.zmatrix

import io.circe.generic.extras.Configuration

package object models {
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
}

import mill._
import scalalib._
import publish._
import mill.scalalib._

import $ivy.`com.goyeau::mill-scalafix::0.2.11`
import com.goyeau.mill.scalafix.ScalafixModule

object Versions {
  val zioLoggingVersion     = "2.1.3"
  val zioVersion            = "2.0.3"
  val zioJsonVersion        = "0.3.0"
  val zioConfigVersion      = "3.0.2"
  val sttpVersion           = "3.8.3"
  val pureConfigVersion     = "0.17.2"
  val scalafixModuleVersion = "0.6.0"
}

val scalaVersions = List("2.12.17", "2.13.10")

trait Publishable extends PublishModule {
  override def artifactName   = "zmatrix"
  override def publishVersion = "0.3.0"

  override def pomSettings = PomSettings(
    description = "Matrix.org API client written using ZIO",
    organization = "com.bot4s",
    url = "https://github.com/bot4s/zmatrix",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("bot4s", "zmatrix"),
    developers = Seq(
      Developer("ex0ns", "ex0ns", "https://github.com/ex0ns")
    )
  )
}

trait ExtendedCrossScalaModule extends CrossScalaModule with ScalafixModule {
  def scalafixIvyDeps = Agg(ivy"com.github.liancheng::organize-imports:${Versions.scalafixModuleVersion}")
}

object core extends Cross[CoreModule](scalaVersions: _*)

class CoreModule(val crossScalaVersion: String) extends ExtendedCrossScalaModule with Publishable {

  import Versions._

  override def ivyDeps = Agg(
    ivy"dev.zio::zio:${zioVersion}",
    ivy"dev.zio::zio-json:${zioJsonVersion}",
    ivy"dev.zio::zio-config:${zioConfigVersion}",
    ivy"dev.zio::zio-config-magnolia:${zioConfigVersion}",
    ivy"dev.zio::zio-config-typesafe:${zioConfigVersion}",
    ivy"dev.zio::zio-logging:${zioLoggingVersion}",
    ivy"com.softwaremill.sttp.client3::core:${sttpVersion}",
    ivy"com.softwaremill.sttp.client3::zio-json:${sttpVersion}",
    ivy"com.softwaremill.sttp.client3::zio:${sttpVersion}"
  )

  override def scalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-Ywarn-unused",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Ywarn-dead-code"
  )

  object test extends Tests with ScalafixModule {
    def ivyDeps = Agg(
      ivy"dev.zio::zio-test:${zioVersion}",
      ivy"dev.zio::zio-test-sbt:${zioVersion}"
    )

    def scalafixIvyDeps = Agg(ivy"com.github.liancheng::organize-imports:${Versions.scalafixModuleVersion}")

    def testOne(spec: String, args: String*) = T.command {
      super.runMain(spec, args: _*)
    }

    def testFramework = "zio.test.sbt.ZTestFramework"

  }
}

object examples extends Cross[ExamplesModule](scalaVersions: _*)
class ExamplesModule(val crossScalaVersion: String) extends ExtendedCrossScalaModule {
  val moduleDeps = Seq(core(crossScalaVersion))
  override def scalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-Ywarn-unused",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Ywarn-dead-code"
  )

  def mainClass = Some("com.bot4s.zmatrix.Runner")
}

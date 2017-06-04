import com.typesafe.config.ConfigFactory
import scoverage.ScoverageKeys
import scala.util.{Try, Success, Failure}

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

name := """deversity-frontend"""
version := btVersion
scalaVersion := "2.11.11"
organization := "com.cjww-dev.frontends"

lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(playSettings ++ scoverageSettings : _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
    parallelExecution in IntegrationTest := false)

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;views.*;models.*;config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

PlayKeys.devSettings := Seq("play.server.http.port" -> "9986")

val cjwwDep: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs" % "data-security_2.11" % "1.2.0",
  "com.cjww-dev.libs" % "http-verbs_2.11" % "1.7.0",
  "com.cjww-dev.libs" % "logging_2.11" % "0.7.0",
  "com.cjww-dev.libs" % "authorisation_2.11" % "1.3.0",
  "com.cjww-dev.libs" % "bootstrapper_2.11" % "1.6.0",
  "com.cjww-dev.libs" % "application-utilities_2.11" % "0.5.0"
)

val testDep: Seq[ModuleID] = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "org.mockito" % "mockito-core" % "2.8.9" % Test
)

libraryDependencies ++= cjwwDep
libraryDependencies ++= testDep

routesGenerator := InjectedRoutesGenerator

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "cjww-dev" at "http://dl.bintray.com/cjww-development/releases"

herokuAppName in Compile := "cjww-deversity"

bintrayOrganization := Some("cjww-development")
bintrayReleaseOnPublish in ThisBuild := true
bintrayRepository := "releases"
bintrayOmitLicense := true

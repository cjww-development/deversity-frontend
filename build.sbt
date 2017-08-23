import com.typesafe.config.ConfigFactory
import scoverage.ScoverageKeys
import scala.util.{Try, Success, Failure}

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

name          := """deversity-frontend"""
version       := btVersion
scalaVersion  := "2.11.11"
organization  := "com.cjww-dev.frontends"

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

lazy val scoverageSettings = Seq(
  ScoverageKeys.coverageExcludedPackages  := "<empty>;Reverse.*;models/.data/..*;views.*;models.*;config.*;.*(AuthService|BuildInfo|Routes).*",
  ScoverageKeys.coverageMinimum           := 80,
  ScoverageKeys.coverageFailOnMinimum     := false,
  ScoverageKeys.coverageHighlighting      := true
)

PlayKeys.devSettings := Seq("play.server.http.port" -> "9986")

val cjwwDep: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs" % "data-security_2.11"          % "2.6.1",
  "com.cjww-dev.libs" % "http-verbs_2.11"             % "2.2.0",
  "com.cjww-dev.libs" % "authorisation_2.11"          % "1.10.0",
  "com.cjww-dev.libs" % "application-utilities_2.11"  % "2.0.1",
  "com.cjww-dev.libs" % "metrics-reporter_2.11"       % "0.3.0"
)

val codeDep: Seq[ModuleID] = Seq(
  "com.kenshoo" % "metrics-play_2.10" % "2.4.0_0.4.0"
)

val testDep: Seq[ModuleID] = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % Test,
  "org.mockito"             % "mockito-core"       % "2.8.47" % Test
)

libraryDependencies ++= cjwwDep
libraryDependencies ++= codeDep
libraryDependencies ++= testDep
libraryDependencies +=  filters


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "cjww-dev" at "http://dl.bintray.com/cjww-development/releases"

herokuAppName in Compile := "cjww-deversity"

bintrayOrganization                   := Some("cjww-development")
bintrayReleaseOnPublish in ThisBuild  := true
bintrayRepository                     := "releases"
bintrayOmitLicense                    := true

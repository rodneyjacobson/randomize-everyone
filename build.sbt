import com.typesafe.sbt.SbtScalariform._

import scalariform.formatter.preferences._

name := "Randomize Everyone"

resolvers += Resolver.jcenterRepo
scalaVersion in ThisBuild := "2.11.8"

lazy val commonSettings = Seq(
  version := "0.1.0",
  scalaVersion := "2.11.8"
)

lazy val client: Project = (project in file("client")).settings(
  //scalaJSUseMainModuleInitializer := true,
  commonSettings,
  unmanagedSourceDirectories in Compile += baseDirectory.value / ".." /"shared",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.1",
    "com.lihaoyi" %%% "scalatags" % "0.4.6",
    "com.lihaoyi" %%% "upickle" % "0.3.6",
    "com.lihaoyi" %%% "scalarx" % "0.3.2",
    "com.lihaoyi" %%% "autowire" % "0.2.6",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.3"
  )
).enablePlugins(ScalaJSPlugin)

lazy val root = (project in file(".")).settings(
  commonSettings,
  unmanagedSourceDirectories in Compile += baseDirectory.value / "shared",
  libraryDependencies ++= Seq(
    "com.mohiva" %% "play-silhouette" % "4.0.0",
    "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0",
    "com.mohiva" %% "play-silhouette-persistence" % "4.0.0",
    "com.lihaoyi" %% "upickle" % "0.3.6",
    "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0",
    "org.webjars" %% "webjars-play" % "2.5.0-4",
    "net.codingwell" %% "scala-guice" % "4.0.1",
    "com.iheart" %% "ficus" % "1.2.6",
    "com.typesafe.play" %% "play-mailer" % "5.0.0",
    "com.enragedginger" %% "akka-quartz-scheduler" % "1.5.0-akka-2.4.x",
    "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3",
    "com.mohiva" %% "play-silhouette-testkit" % "4.0.0" % "test",
    "org.postgresql" % "postgresql" % "9.4.1212",
    "com.typesafe.play" %% "play-slick" % "2.0.0",
    "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
    "com.lihaoyi" %%% "autowire" % "0.2.6",
    "com.atlassian.commonmark"   % "commonmark"  % "0.7.0",
    "org.apache.directory.api"   % "api-all" % "1.0.0",
    specs2 % Test,
    cache,
    filters
  )
).enablePlugins(PlayScala)

routesGenerator := InjectedRoutesGenerator

routesImport += "utils.route.Binders._"

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(DanglingCloseParenthesis, Preserve)

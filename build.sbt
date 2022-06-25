// Versions
ThisBuild / scalaVersion := "3.1.2"
ThisBuild / version      := "0.1"

val circeVersion      = "0.14.1"
val munitCEVersion    = "1.0.7"
val http4sVersion     = "1.0.0-M32"
val httpClientVersion = "1.0.0-M1"

val lwjglVersion = "3.1.6"
val os           = "windows" // TODO: Change to "linux" or "macos" if necessary

// Main build
lazy val root = (project in file("."))
  .settings(
    // App
    name         := "fabled-kingdoms",
    organization := "wafflepop.games",

    // Editor
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,

    // Dependencies
    libraryDependencies ++= Seq(
      "io.circe"      %% "circe-core"             % circeVersion,
      "io.circe"      %% "circe-generic"          % circeVersion,
      "io.circe"      %% "circe-parser"           % circeVersion,
      "org.http4s"    %% "http4s-client"          % http4sVersion,
      "org.http4s"    %% "http4s-dsl"             % http4sVersion,
      "org.http4s"    %% "http4s-ember-client"    % http4sVersion,
      "org.http4s"    %% "http4s-jdk-http-client" % httpClientVersion,
      "org.typelevel" %% "munit-cats-effect-3"    % munitCEVersion % Test
    ),
    libraryDependencies ++= Seq(
      "lwjgl",
      "lwjgl-glfw",
      "lwjgl-opengl"
      // Add more modules here
    ).flatMap { module =>
      {
        Seq(
          "org.lwjgl" % module % lwjglVersion,
          "org.lwjgl" % module % lwjglVersion classifier s"natives-$os"
        )
      }
    }
  )

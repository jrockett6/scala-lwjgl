Compile / run / fork := true

// Versions
ThisBuild / scalaVersion := "3.1.2"
ThisBuild / version      := "0.1"
// Compile / run / mainClass := Some("wafflepop.go")

val circeVersion      = "0.14.1"
val guavaVersion      = "31.1-jre"
val http4sVersion     = "1.0.0-M32"
val httpClientVersion = "1.0.0-M1"
val munitCEVersion    = "1.0.7"

val lwjglVersion = "3.3.1"
val os           = "macos" // Change to "linux" or "macos" if necessary

lazy val setup: TaskKey[Unit] = taskKey[Unit]("Run setup script")
lazy val hello                = taskKey[Unit]("An example task")

hello := { println("Hello!") }

// Main build
lazy val root = (project in file("."))
  .settings(
    setup := {
      import sys.process._
      Seq("./setup.sh") !
    },

    // App
    name         := "fabled-kingdoms",
    organization := "wafflepop.games",

    // Editor
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,

    // Dependencies
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava"                  % guavaVersion,
      "io.circe"        %% "circe-core"             % circeVersion,
      "io.circe"        %% "circe-generic"          % circeVersion,
      "io.circe"        %% "circe-parser"           % circeVersion,
      "org.http4s"      %% "http4s-client"          % http4sVersion,
      "org.http4s"      %% "http4s-dsl"             % http4sVersion,
      "org.http4s"      %% "http4s-ember-client"    % http4sVersion,
      "org.http4s"      %% "http4s-jdk-http-client" % httpClientVersion,
      "org.typelevel"   %% "munit-cats-effect-3"    % munitCEVersion % Test,
      "org.lwjgl"        % "lwjgl"                  % lwjglVersion,
      "org.lwjgl"        % "lwjgl"                  % lwjglVersion classifier (s"natives-$os"),
      "org.lwjgl"        % "lwjgl-glfw"             % lwjglVersion,
      "org.lwjgl"        % "lwjgl-glfw"             % lwjglVersion classifier (s"natives-$os"),
      "org.lwjgl"        % "lwjgl-vulkan"           % lwjglVersion
      // "org.lwjgl"        % "lwjgl-vulkan"           % lwjglVersion classifier s"natives-$os"
    ),
    javaOptions ++= Seq(
      "-XstartOnFirstThread",
      "-Dorg.lwjgl.vulkan.libname=/Users/jrockett/myworkspace/VulkanSDK/1.3.204.1/macOS/lib/libvulkan.dylib"
    )
  )

Compile / compile := ((Compile / compile) dependsOn setup).value

name := "freetype"

version := "0.0.4"

versionScheme := Some("early-semver")

scalaVersion := "3.7.0"

enablePlugins(ScalaNativePlugin)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:existentials",
)

organization := "io.github.edadma"

Global / onChangedBuildSource := ReloadOnSourceChanges

resolvers += Resolver.githubPackages("edadma")

licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/edadma/" + name.value))

libraryDependencies += "io.github.edadma" %%% "freetype_face" % "0.0.2"

publishMavenStyle := true

Test / publishArtifact := false

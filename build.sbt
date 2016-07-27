
organization := "com.superpixel"

name := "jdot"

version := "0.0.3-SNAPSHOT"
isSnapshot := true

scalaVersion := "2.11.7"

scalacOptions += "-feature"

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.github.nscala-time" %% "nscala-time" % "2.10.0",
  "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

publishMavenStyle := true
publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))

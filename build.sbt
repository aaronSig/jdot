
organization := "com.superpixel"

name := "jdot"

version := "0.0.3-SNAPSHOT"
isSnapshot := true

scalaVersion := "2.11.7"

scalacOptions += "-feature"

EclipseKeys.withSource := true

libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.3.0"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.10.0"
libraryDependencies += "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final"
libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

publishMavenStyle := true
publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))

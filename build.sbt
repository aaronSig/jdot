
organization := "com.superpixel.advokit"

name := "content-mapper"

version := "0.0.1-SHAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.6+"
libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"


publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))

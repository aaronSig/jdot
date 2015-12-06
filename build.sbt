
organization := "com.superpixel.advokit"

name := "content-mapper"

version := "0.0.1-SHAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.3.0"
libraryDependencies += "org.json4s" %% "muster-codec-jackson" % "0.3.0"
libraryDependencies += "org.json4s" %% "muster-codec-json4s" % "0.3.0"
libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"


publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))

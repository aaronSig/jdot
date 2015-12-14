package com.superpixel.advokit.mapper


sealed trait Inclusions;
case class FixedInclusions(inc: Map[String, String]) extends Inclusions;
case object NoInclusions extends Inclusions;


sealed trait DefaultJson;
case class DefaultJsonIn(in: String) extends DefaultJson
case class DefaultJsonOut(out: String) extends DefaultJson
case class DefaultJsonInOut(in: String, out: String) extends DefaultJson
case object NoDefaultJson extends DefaultJson
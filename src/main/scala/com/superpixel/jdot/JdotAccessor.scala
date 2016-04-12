package com.superpixel.jdot

import com.superpixel.jdot.pathing.JPath
import com.superpixel.jdot.json4s.JValueAccessor

trait JDotAccessor {

  def getNumber(jPath: JPath): Option[Number]
  def getBoolean(jPath: JPath): Option[Boolean]
  def getString(jPath: JPath): Option[String]
  def getValueAsString(jPath: JPath): Option[String]
  def getJsonString(jPath: JPath): Option[String]
  
}

object JDotAccessor {
  import org.json4s.native.JsonMethods._
  def apply(json: String): JDotAccessor = JValueAccessor(parse(json));
  def apply(json: String, linkLamb: String=>Option[String]): JDotAccessor = {
    val jvLinkLamb = (key: String) => linkLamb(key).map { jsonString => parse(jsonString) }
    JValueAccessor(parse(json), jvLinkLamb)
  }
  def apply(json: String, inclusions: Inclusions): JDotAccessor = {
    val jvLinkLamb = inclusions match {
      case NoInclusions => (key: String) => None
      case FixedInclusions(mp) => (key: String) => mp.get(key).map { jsonString => parse(jsonString) }
    }
    JValueAccessor(parse(json), jvLinkLamb)
  }
}
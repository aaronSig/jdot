package com.superpixel.jdot.json4s

import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.jdot.json4s.JValueTraverser.Continue
import com.superpixel.jdot.pathing._
import com.superpixel.jdot.JDotAccessor

class JValueAccessor(json: JValue, linkLamb: String=>Option[JValue]) extends JDotAccessor {

	private val traverseForValue = JValueTraverser.traverse(linkLamb, JValueAccessor.notFoundLamb, JValueAccessor.endLamb)_

  def getValue(jPath: JPath): JValue = {
    try {
      traverseForValue(json, jPath).getOrElse(JNothing)
    } catch {
      case e: JsonTraversalException => {
        println("ERROR: " + e.getMessage)
        println("    for JPath: " + jPath.toString())
        println("    for JSON: " + (if (json == JNothing) "nothing" else compact(render(json))))
        return JNothing
      }
    }
  }
  
  
  override def getNumber(jPath: JPath): Option[Number] = this.getValue(jPath) match {
    case JNothing | JNull => None
    case JDouble(db) => Some(db)
    case JInt(i) => Some(i)
    case JLong(l) => Some(l)
    case JDecimal(dec) => Some(dec)
    case _ => None
  }
  
  override def getBoolean(jPath: JPath): Option[Boolean] = this.getValue(jPath) match {
    case JNothing | JNull => None
    case JBool(b) => Some(b)
    case _ => None
  }
  
  override def getString(jPath: JPath): Option[String] = this.getValue(jPath) match {
    case JNothing | JNull => None
    case JString(s) => Some(s)
    case _ => None
  }
  
  override def getValueAsString(jPath: JPath): Option[String] = this.getValue(jPath) match {
    case JNothing | JNull => None
    case _:JArray | _:JObject => None
    case value => Some(compact(render(value)))
  }
  
  override def getJsonString(jPath: JPath): Option[String] = this.getValue(jPath) match {
    case JNothing | JNull => None
    case value => Some(compact(render(value)))
  }
   
}


object JValueAccessor {
  
  def apply(json: JValue, linkLamb: String=>Option[JValue] = (s)=>None): JValueAccessor = new JValueAccessor(json, linkLamb)
  
  private val notFoundLamb = (jVal: JValue, jPath: JPathElement) => {
    (JNothing, Continue)
  }
  private val endLamb: PartialFunction[Tuple2[JValue, Option[JPathElement]], JValue] = 
    (JValueTraverser.jDefaultValueNoComplexEnd andThen { case s: String => JString(s) }) orElse 
    { case (jVal, _) => jVal }
}
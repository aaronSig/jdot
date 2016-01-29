package com.superpixel.advokit.json.lift

import com.superpixel.advokit.json.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.lift.JValueTraverser.{RouteChoice, Continue, Stop}

class JValueAccessor(json: JValue, linkLamb: String=>Option[JValue]) {

	private val traverseForValue = JValueTraverser.traverse(linkLamb, JValueAccessor.notFoundLamb, JValueAccessor.endLamb)_

  def getValue(jPath: JPath): JValue = traverseForValue(json, jPath).getOrElse(JNothing)
   
}


object JValueAccessor {
  
  def apply(json: JValue, linkLamb: String=>Option[JValue] = (s)=>None): JValueAccessor = new JValueAccessor(json, linkLamb)
  
  private val notFoundLamb = (jVal: JValue, jPath: JPathElement) => {
    println("NOT FOUND: " + jPath)
    (JNothing, Continue)
  }
  private val endLamb: PartialFunction[Tuple2[JValue, Option[JPathElement]], JValue] = 
    (JValueTraverser.jDefaultValueNoComplexEnd andThen { case s: String => JString(s) }) orElse 
    { case (jVal, _) => jVal }
  
}
package com.superpixel.advokit.json.lift

import com.superpixel.advokit.json.pathing._
import net.liftweb.json._

class JValueExtractor(json: JValue, linkLamb: String=>Option[JValue]) {
  
	private val notFoundLamb = (jVal: JValue, jPath: JPathElement) => throw new JsonTraversalException(s"Could not find path: $jPath", jVal)
	private val endLamb = (jVal: JValue, jPathOpt: Option[JPathElement]) => jVal

	private val traverseForValue = traverse(linkLamb, notFoundLamb, endLamb)_

  def getValue(jPath: JPath): JValue = traverseForValue(json, jPath)
  
  //TODO move to own object
  private def traverse[T](linkLamb: String=>Option[JValue],  
                       notFoundLamb: (JValue, JPathElement)=>JValue,
                       endLamb: (JValue, Option[JPathElement])=>T)
                      (jVal: JValue, jPath: JPath): T = {
    
    
    def traverseRecu(value: JValue, pathSeq: Seq[JPathElement], prev: Option[JPathElement] = None): T = {
      def routeValueOption(valOpt: Option[JValue], hd: JPathElement, tl: JPath):T = {
          valOpt match {
          case None => traverseRecu(notFoundLamb(value, hd), tl, Some(hd))
          case Some(jV) => traverseRecu(jV, tl, Some(hd))
          } 
      }
      pathSeq match {
        case Nil => endLamb(value, prev)
        
        case JObjectPath(key) +: tl => routeValueOption(accessJObjectValue(value, key), pathSeq.head, tl)
        
        case JArrayPath(key) +: tl => routeValueOption(accessJArrayValue(value, key), pathSeq.head, tl)
        
        case JPathLink +: tl => value match {
          case JString(s) => routeValueOption(linkLamb(s), pathSeq.head, tl)
          case jV => throw new JsonTraversalException(s"Expected string JSON value in order to following inclusions link, instead found the following", jV)
        }
        
      }
    }
    
    traverseRecu(jVal, jPath) 
  }
  
  private def accessJArrayValue(jVal: JValue, key: Int): Option[JValue] = jVal match {
    case JObject(fieldLs) => fieldLs.find { jFld => jFld.name == key.toString } .map(_.value)
    case JArray(valueLs) => valueLs.lift(key)
    case _ => throw new JsonTraversalException(s"Path requested access to key [$key], but JSON is not an Object or Array", jVal)
  }
  
  private def accessJObjectValue(jVal: JValue, key: String): Option[JValue] = jVal match {
    case JObject(fieldLs) => fieldLs.find { jFld => jFld.name == key } .map(_.value)
    case _ => throw new JsonTraversalException(s"Path requested access to key ('$key'), but JSON is not an Object", jVal)
  }
  
}

class JsonTraversalException(message: String, jVal: JValue = JNull) 
    extends RuntimeException(s"$message. For Json:\n " + pretty(render(jVal)))

object JValueExtractor {
  
  def apply(json: JValue, linkLamb: String=>Option[JValue] = (s)=>None): JValueExtractor = new JValueExtractor(json, linkLamb)
  
}
package com.superpixel.advokit.json.lift

import com.superpixel.advokit.json.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._

class JValueExtractor(json: JValue, linkLamb: String=>Option[JValue]) {
  
	private val notFoundLamb = (jVal: JValue, jPath: JPathElement) => (JNothing, Continue)
	private val endLamb = (jVal: JValue, jPathOpt: Option[JPathElement]) => jVal

	private val traverseForValue = traverse(linkLamb, notFoundLamb, endLamb)_

  def getValue(jPath: JPath): JValue = traverseForValue(json, jPath)
  
  
  
  private sealed trait RouteChoice;
  private case object Stop extends RouteChoice;
  private case object Continue extends RouteChoice;
  //TODO move to own object
  private def traverse[T](linkLamb: String=>Option[JValue],  
                       notFoundLamb: (JValue, JPathElement)=>(JValue, RouteChoice),
                       endLamb: (JValue, Option[JPathElement])=>T)
                      (jVal: JValue, jPath: JPath): T = {
    
    
    def traverseRecu(value: JValue, pathSeq: Seq[JPathElement], prev: Option[JPathElement] = None): T = {
      def routeValueOption(valOpt: Option[JValue], hd: JPathElement, tl: JPath):T = {
          valOpt match {
          case None => notFoundLamb(value, hd) match {
            case (jV, Stop) => return endLamb(jV, Some(hd))
            case (jV, Continue) => traverseRecu(jV, tl, Some(hd))
          } 
          case Some(jV) => traverseRecu(jV, tl, Some(hd))
          } 
      }
      
      pathSeq match {
        case Nil => endLamb(value, prev)
        
        case JObjectPath(key) +: tl => routeValueOption(accessJObjectValue(value, key), pathSeq.head, tl)
        
        case JArrayPath(key) +: tl => routeValueOption(accessJArrayValue(value, key), pathSeq.head, tl)
        
        case JPathLink +: tl => 
          routeValueOption(
            value match {
              case JString(s) => linkLamb(s)
              case JInt(d) =>  linkLamb(d.toString)
              case _ => None
            }, pathSeq.head, tl)
        
        case JDefaultValue(dVal) +: tl => value match {
          case JNothing => endLamb(JString(dVal), Some(pathSeq.head))
          case jV => routeValueOption(Some(jV), pathSeq.head, tl)
        }
      }
    }
    
    traverseRecu(jVal, jPath) 
  }
  
  private def accessJArrayValue(jVal: JValue, key: Int): Option[JValue] = jVal match {
    case JObject(fieldLs) => fieldLs.find { jFld => jFld._1 == key.toString } .map(_._2)
    case JArray(valueLs) => valueLs.lift(key)
    case _ => None
  }
  
  private def accessJObjectValue(jVal: JValue, key: String): Option[JValue] = jVal match {
    case JObject(fieldLs) => fieldLs.find { jFld => jFld._1 == key } .map(_._2)
    case _ => None
  }
  
}

class JsonTraversalException(message: String, jVal: JValue = JNull) 
    extends RuntimeException(s"$message. For Json:\n " + pretty(render(jVal)))

object JValueExtractor {
  
  def apply(json: JValue, linkLamb: String=>Option[JValue] = (s)=>None): JValueExtractor = new JValueExtractor(json, linkLamb)
  
}
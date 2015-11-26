package com.superpixel.advokit.json.mapping

import net.liftweb.json._
import com.superpixel.advokit.json.pathing._

class JsonTransformer(fieldMap: Set[JPathMap], inclusions: Map[String, JValue]) {

  
  def transform(json: JValue, addInclusions: Map[String, JValue] = Map()): JValue = 
    builder(getValues(json, addInclusions))
  
  private def getValues(json: JValue, addInclusions: Map[String, JValue]): Set[(JPath, JValue)] = {
    val linkLamb = (s: String) => addInclusions.get(s).orElse(inclusions.get(s))
    val notFoundLamb = (jVal: JValue, jPath: JPath) => throw new JsonTraversalException(s"Could not find path: $jPath", jVal)
    val endLamb = (jVal: JValue, jPath: JPath) => jVal
    
    val trav = traverse(linkLamb, notFoundLamb, endLamb)_
    
    fieldMap.map { jpm: JPathMap => (jpm.to, trav(json, jpm.from)) }
  }
  
  private def builder(pathValues: Set[(JPath, JValue)]): JValue = ???
  
  private def traverse[T](linkLamb: String=>Option[JValue],  
                       notFoundLamb: (JValue, JPath)=>JValue,
                       endLamb: (JValue, JPath)=>T)
                      (jVal: JValue, jPath: JPath): T = {
    
    def traverseRecu(value: JValue, path: JPath, parent: Option[JPath] = None): T = {      
      path match {
        case JPathTerminus => endLamb(value, parent.get)
        
        case JAccess(key, child) => accessJValue(value, key) match {
          case None => traverseRecu(notFoundLamb(value, path), child, Some(path))
          case Some(jV) => traverseRecu(jV, child, Some(path))
        } 
        
        case JPathLink(child) => value match {
          case JString(s) => linkLamb(s)  match {
            case None => traverseRecu(notFoundLamb(value, path), child, Some(path))
            case Some(jV) => traverseRecu(jV, child, Some(path))
          }
          case jV => throw new JsonTraversalException(s"Expected string JSON value in order to following inclusions link, instead found the following", jV)
        }
      }
    }
      
    
    (jVal, jPath) match {
      case (_, JPathTerminus) => throw new JsonTraversalException(s"JPath contains only a terminus, no mapping available", jVal)
      case (JObject(_) | JArray(_), _) => traverseRecu(jVal, jPath)
      case (_ | _) => throw new JsonTraversalException(s"Top level of JSON must be either an Array or an Object", jVal)
    } 
  }
  
  private def accessJValue(jVal: JValue, key: String): Option[JValue] = jVal match {
      case JObject(fieldLs) => fieldLs.find { jFld => jFld.name == key } .map(_.value)
      case JArray(valueLs) => {
        try { 
          val i = key.toInt 
      		if (i >= 0 && i < valueLs.size) Some(valueLs(i)) else None        
        } catch {
          case e: NumberFormatException => throw new JsonTraversalException(s"Found JSON Array, but access key was not a number ('$key')", jVal)
        }
      }
      case _ => throw new JsonTraversalException(s"Path requested access to key ('$key'), but JSON is not an Object or Array", jVal)
  }
}

class JsonTraversalException(message: String, jVal: JValue = JNull) 
    extends RuntimeException(s"$message. For Json:\n " + pretty(render(jVal)))

object JsonTransformer {
  
  def apply(fieldMap: Set[JPathMap], inclusions: Map[String, JValue]) = new JsonTransformer(fieldMap, inclusions);
  
}
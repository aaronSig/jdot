package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._
import scala.collection.JavaConverters._

class JavaListSerializer extends Serializer[java.util.List[_]] {
   private val ListClass = classOf[java.util.List[_]]

   def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), java.util.List[_]] = {
     case (TypeInfo(ListClass, _), json) => json.extract[List[_]].asJava
   }

   def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
     case x: java.util.List[_] =>
       JArray(List())
   }
 }
package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._
import scala.collection.JavaConverters._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

class JavaOptionalSerializer extends Serializer[java.util.Optional[_]] {
    private val OptionalClass = classOf[java.util.Optional[_]]

   def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), java.util.Optional[_]] = {
     case (TypeInfo(OptionalClass, _), json) => {
       json.extract[Option[_]] match {
         case Some(obj) => java.util.Optional.of(obj)
         case None => java.util.Optional.empty()
       }
       
     }
   }

   def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
     case x: java.util.Optional[_] =>
       if (x.isPresent) {
         Extraction.decompose(x.get())
       } else {
         JNothing
       }
   }
}
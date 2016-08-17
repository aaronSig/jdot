package com.superpixel.jdot.json4s

import org.json4s._
import org.json4s.native.JsonMethods._
import scala.collection.JavaConverters._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

class JavaOptionalSerializer extends Serializer[com.google.common.base.Optional[_]] {
    private val OptionalClass = classOf[com.google.common.base.Optional[_]]

   def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), com.google.common.base.Optional[_]] = {
     case (TypeInfo(OptionalClass, _), json) => {
       json.extract[Option[_]] match {
         case Some(obj) => com.google.common.base.Optional.of(obj)
         case None => com.google.common.base.Optional.absent()
       }
       
     }
   }

   def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
     case x: com.google.common.base.Optional[_] =>
       if (x.isPresent) {
         Extraction.decompose(x.get())
       } else {
         JNothing
       }
   }
}
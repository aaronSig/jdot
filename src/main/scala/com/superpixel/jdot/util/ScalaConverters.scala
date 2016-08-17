package com.superpixel.jdot.util
import scala.collection.JavaConverters._
import com.superpixel.jdot.pathing.JPathPair


object ScalaConverters {
  
  def scOptionToJvOptional[T](scOption: scala.Option[T]): com.google.common.base.Optional[T] =  scOption match {
    case None => com.google.common.base.Optional.absent();
    case Some(t) => com.google.common.base.Optional.of(t);
  }

  def jvOptionalToScOption[T](jvOptional: com.google.common.base.Optional[T]): scala.Option[T] = {
    if (jvOptional.isPresent) Some(jvOptional.get())
    else None
  }

  def scOptionBooleanToJvOptionalBoolean(scOption: scala.Option[scala.Boolean]): com.google.common.base.Optional[java.lang.Boolean] =  scOption match {
    case None => com.google.common.base.Optional.absent();
    case Some(b) => com.google.common.base.Optional.of(scTojvBoolean(b));
  }

  def scTojvBoolean(b: scala.Boolean): java.lang.Boolean = {
    return new java.lang.Boolean(b);
  }

  def jvToScMap[U, V](jvMap: java.util.Map[U, V]): scala.collection.immutable.Map[U, V] = {
    if (jvMap == null) {
      scala.collection.immutable.Map[U, V]()
    } else {
      jvMap.asScala.toMap
    }
  }
  
  def jvListToScSeq[T](jvList: java.util.List[T]): scala.collection.Seq[T] = {
    if (jvList == null) {
      Nil
    } else {
      jvList.asScala.toSeq
    }
  }
  
  def jvToScList[T](jvList: java.util.List[T]): scala.collection.immutable.List[T] = {
    if (jvList == null) {
      Nil
    } else {
      jvList.asScala.toList
    }
  }
  
  def jvArrayToScSeq[T](jvArray: Array[T]): scala.collection.Seq[T] = {
    jvArray
  }
  
  def jvStringMapToJPathPairSet(jvMap: java.util.Map[String, String]): scala.collection.immutable.Set[JPathPair] = {
    if (jvMap == null) {
      scala.collection.immutable.Set[JPathPair]();
    } else {
      val entrySet: collection.immutable.Set[(String, String)] = jvMap.asScala.toSet
      entrySet.map { case (to:String , from:String) => JPathPair.fromStrings(to, from) }
    }
  }

}
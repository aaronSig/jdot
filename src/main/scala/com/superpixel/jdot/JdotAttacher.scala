package com.superpixel.jdot

import com.superpixel.jdot.pathing.JPathPair
import com.superpixel.jdot.json4s.JValueAttacher

/***
  *
  *
  */
trait JDotAttacher {

  def attach(contextJson: String, attachToJson: String): String
  
  def attachList(contextJsonList: List[String], attachToJson: String): String
  
}

object JDotAttacher {
  def apply(attachmentPairs: Set[JPathPair],
            attachmentContext: AttachmentContext = SimpleAttachmentContext,
            treatArraysAsList: Boolean = true,
            transformer: Option[JDotTransformer] = None,
            nestedAttachers: List[JDotAttacher] = Nil): JDotAttacher = JValueAttacher(attachmentPairs)
}
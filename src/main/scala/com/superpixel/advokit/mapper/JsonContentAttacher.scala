package com.superpixel.advokit.mapper

import com.superpixel.advokit.json.pathing.JPathPair
import com.superpixel.advokit.json.lift.JValueAttacher

trait JsonContentAttacher {

  def attach(attachJson: String, attachToJson: String): String
  
  def attachList(attachJsonList: List[String], attachToJson: String): String
  
}

object JsonContentAttacher {
  def apply(attachmentPairs: Set[JPathPair]): JsonContentAttacher = JValueAttacher(attachmentPairs)
}
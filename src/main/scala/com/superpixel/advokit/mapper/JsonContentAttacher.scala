package com.superpixel.advokit.mapper

trait JsonContentAttacher {

  def attach(attachJson: String, attachToJson: String): String
  
  def attachList(attachJsonList: List[String], attachToJson: String): String
  
}
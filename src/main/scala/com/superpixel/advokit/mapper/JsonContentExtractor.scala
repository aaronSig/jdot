package com.superpixel.advokit.mapper

trait JsonContentExtractor[T] {

  def extract(json: String): T
  
}
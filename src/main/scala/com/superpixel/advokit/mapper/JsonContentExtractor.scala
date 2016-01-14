package com.superpixel.advokit.mapper

trait JsonContentExtractor[T] {

  def extract(json: String): T
  
  def extractorWithTypeHints(localTypeHints: List[Class[_]] = Nil):( String => T )
}
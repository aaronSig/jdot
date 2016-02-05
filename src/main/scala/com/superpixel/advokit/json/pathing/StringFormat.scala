package com.superpixel.advokit.json.pathing

sealed trait StringFormat
case class FormatLiteral(literal: String) extends StringFormat
case object ReplaceHolder extends StringFormat

class StringFormatException(message: String, formatSeq: Seq[StringFormat], formatStrings: Seq[String], cause: Throwable = null) 
  extends RuntimeException(s"$message IN formatSequence: $formatSeq AND formatStrings: $formatStrings", cause)

object StringFormat {
  
  def formatToString(sfSeq: Seq[StringFormat], strings: Seq[String]): String = {
    def inner(seq: Seq[StringFormat], strs: Seq[String], acc: Seq[String]): Seq[String] = seq match {
      case Nil => acc.reverse
      case (ReplaceHolder +: tl) => 
        inner(tl, strs.tail, strs.headOption.getOrElse {
          throw new StringFormatException("Insufficient number of formatStrings ", sfSeq, strings)
        } +: acc)
      case (FormatLiteral(str) +: tl) => inner(tl, strs, str +: acc)
    }
    
    inner(sfSeq, strings, Nil).mkString
  }
  
}
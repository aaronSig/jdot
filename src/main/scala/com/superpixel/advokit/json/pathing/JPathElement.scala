package com.superpixel.advokit.json.pathing

import java.util.regex.Pattern
import scala.util.matching.Regex

sealed trait JPathElement;

case object JPathLink extends JPathElement

case class JObjectPath(key: String) extends JPathElement

case class JArrayPath(idx: Int) extends JPathElement

case class JDefaultValue(value: String) extends JPathElement

case class JStringFormat(formatSeq: Seq[StringFormat], valuePaths: Seq[JPath]) extends JPathElement

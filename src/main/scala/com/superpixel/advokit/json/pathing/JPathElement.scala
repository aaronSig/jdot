package com.superpixel.advokit.json.pathing

import java.util.regex.Pattern
import scala.util.matching.Regex

abstract sealed trait JPathElement;

case object JPathLink extends JPathElement
case class JObjectPath(val key: String) extends JPathElement
case class JArrayPath(val idx: Int) extends JPathElement




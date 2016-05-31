package com.superpixel.jdot.pathing

import java.util.regex.Pattern
import scala.util.matching.Regex

sealed trait JPathElement;

case object JPathLink extends JPathElement

case class JObjectPath(objKey: ObjectKey) extends JPathElement

sealed trait ObjectKey;
case class LiteralKey(key: String) extends ObjectKey;
case class KeyFromPath(keyPath: JPath) extends ObjectKey;


case class JArrayPath(arrIdx: ArrayIndex) extends JPathElement

sealed trait ArrayIndex;
case class LiteralIndex(idx: Int) extends ArrayIndex;
case class IndexFromPath(idxPath: JPath) extends ArrayIndex;


case class JPathValue(value: String, transmutation: Option[JTransmute]) extends JPathElement

case class JDefaultValue(value: String, transmutation: Option[JTransmute]) extends JPathElement

case class JStringFormat(formatSeq: Seq[StringFormat], valuePaths: Seq[JPath]) extends JPathElement


case class JConditional(conditionPath: JPath, testPath: Option[JPath], truePath: JPath, falsePath: JPath) extends JPathElement


case class JTransmute(transmuteType: String, argument: Option[TransmuteArgument]) extends JPathElement

sealed trait TransmuteArgument;
case class LiteralArgument(str: String) extends TransmuteArgument;
case class NestedArgument(path: JPath) extends TransmuteArgument;
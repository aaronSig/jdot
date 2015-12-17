package com.superpixel.advokit.mapper


sealed trait Inclusions;
case class FixedInclusions(inc: Map[String, String]) extends Inclusions;
case object NoInclusions extends Inclusions;


sealed trait MergingJson;
case class MergingJsonPre(pre: Seq[String]) extends MergingJson
case class MergingJsonPost(post: Seq[String]) extends MergingJson
case class MergingJsonPrePost(pre: Seq[String], post: Seq[String]) extends MergingJson
case object NoMerging extends MergingJson
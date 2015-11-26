package com.superpixel.advokit.json.pathing

import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.FlatSpec


class JPathTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  "JPath apply" should "be able to interpret dot syntax" in {

    assertResult(
      JPathAccess("one", JPathAccess("zwei", JPathAccess("trois", JPathTerminus)))) {
        JPath("one.zwei.trois")
      }
  }
  
  it should "be able to interpret square bracket syntax" in {
    assertResult(
      JPathAccess("one", JPathAccess("zwei", JPathAccess("trois", JPathTerminus)))) {
        JPath("one[zwei][trois]")
      }
  }
  
  it should "be able to interpret a mixture syntax" in {
    assertResult(
      JPathAccess("one", JPathAccess("zwei", JPathAccess("trois", JPathAccess("quatro", JPathTerminus))))) {
        JPath("one.zwei[trois][quatro]")
      }
    assertResult(
      JPathAccess("one", JPathAccess("zwei", JPathAccess("trois", JPathAccess("quatro", JPathLink(JObjectPath("cinque", JTerminus))))))) {
        JPath("one[zwei].trois[quatro]>.cinque")
      }
  }

  "JPath validate" should "validate dot syntax" in {
    assertResult(true) {
      JPath.validate("one.zwei.trois")
    }
  }

  it should "validate and flag bad dot syntax (double dot)" in {
    assertResult(false) {
      JPath.validate("one..zwei.trois")
    }
  }

  it should "validate and flag bad dot syntax (leading dot)" in {
    assertResult(false) {
      JPath.validate(".one.zwei.trois")
    }
  }

  it should "validate and flag bad dot syntax (trailing dot)" in {
    assertResult(false) {
      JPath.validate("one.zwei.trois.")
    }
  }

  it should "validate bracket syntax" in {
    assertResult(true) {
      JPath.validate("one[zwei][trois]")
    }
  }

  it should "validate and flag bad bracket syntax (leading bracket)" in {
    assertResult(false) {
      JPath.validate("[one][zwei][trois]")
    }
  }

  it should "validate and flag bad bracket syntax (unopened brackets)" in {
    assertResult(false) {
      JPath.validate("one][zwei][trois]")
    }
  }

  it should "validate and flag bad bracket syntax (unclosed brackets)" in {
    assertResult(false) {
      JPath.validate("one[zwei][[trois]")
    }
  }

  it should "validate and flag bad bracket syntax (nested brackets)" in {
    assertResult(false) {
      JPath.validate("one[zwei][trois[quatro]]")
    }
  }

  it should "validate and flag bad bracket syntax (trailing brackets)" in {
    assertResult(false) {
      JPath.validate("one[zwei][trois][quatro][")
    }
  }

  it should "validate mixed syntax" in {
    assertResult(true) {
      JPath.validate("one.zwei[trois][quatro]")
    }
    assertResult(true) {
      JPath.validate("one[zwei].trois[quatro]")
    }
  }

  it should "validate and flag bad mixed syntax (nested dot)" in {
    assertResult(false) {
      JPath.validate("one[zwei][trois.quatro]")
    }
  }

  it should "validate and flag bad mixed syntax (dot to bracket)" in {
    assertResult(false) {
      JPath.validate("one[zwei].trois.[quatro]")
    }
  }
  
  "JPath unescapeJKey" should "remove escaping from dots" in {
    assertResult("on.e") {
      JPath.unescapeJKey("""on\.e""")
    }
  }
  
  it should "remove escaping from square brackets" in {
    assertResult("o[n]e") {
      JPath.unescapeJKey("""o\[n\]e""")
    }
  }
  
  it should "remove escaping from chevron" in {
    assertResult("on>>e>") {
      JPath.unescapeJKey("""on\>\>e\>""")
    }
  }
  
  it should "remove escaping with multiple backslashes" in {
    assertResult("""on\\>.e\\\\]""") {
      JPath.unescapeJKey("""on\\\>\.e\\\\\]""")
    }
  }
  
  "JPath escapeJKey" should "escape dots" in {
    assertResult("""on\.e""") {
      JPath.escapeJKey("""on.e""")
    }
  }
  
  it should "escape square brackets" in {
    assertResult("""on\]\[e""") {
      JPath.escapeJKey("""on][e""")
    }
  }
  
  it should "escape chevron" in {
    assertResult("""on\>e""") {
      JPath.escapeJKey("""on>e""")
    }
  }
  
  it should "escape cancelled escaped special chars" in {
    assertResult("""on\\\.e""") {
      JPath.escapeJKey("""on\\.e""")
    }
  }
  
  "JPath escapeJKey and unescapeJKey" should "be inverse functions" in {
    val tricky = """>o\\"\\\\.n\\>e.tw\\o't\\]\\[h[ree""";
    assertResult(tricky) {
      JPath.unescapeJKey(JPath.escapeJKey(tricky))
    }
  }

}
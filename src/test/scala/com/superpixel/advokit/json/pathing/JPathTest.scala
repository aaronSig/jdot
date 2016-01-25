package com.superpixel.advokit.json.pathing

import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import com.superpixel.advokit.json.pathing.JPath.StringFormatExpression


class JPathTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  "JPath fromString" should "be able to interpret dot syntax" in {

    assertResult(
      JPath(JObjectPath("one"), JObjectPath("zwei"), JObjectPath("trois"))) {
        JPath.fromString("one.zwei.trois")
      }
  }
  
  it should "be able to interpret square bracket syntax" in {
    assertResult(
      JPath(JObjectPath("one"), JObjectPath("zwei"), JObjectPath("trois"))) {
        JPath.fromString("one[zwei][trois]")
      }
  }
  
  it should "be able to interpret square bracket array syntax" in {
    assertResult(
      JPath(JObjectPath("one"), JArrayPath(2), JArrayPath(3))) {
        JPath.fromString("one[2][3]")
      }
  }
  
  it should "be able to interpret default values" in {
    assertResult(
        JPath(JObjectPath("one"), JObjectPath("zwei"), JDefaultValue("abc"))) {
      JPath.fromString("one.zwei(abc)")
    }
    assertResult(
        JPath(JObjectPath("one"), JDefaultValue("abc"), JObjectPath("zwei"))) {
      JPath.fromString("one(abc).zwei")
    }
  }
  
  it should "be able to interpret a mixture syntax" in {
    assertResult(
      JPath(JObjectPath("one"), JObjectPath("zwei"), JArrayPath(3), JObjectPath("quatro"))) {
        JPath.fromString("one.zwei[3][quatro]")
      }
    assertResult(
      JPath(JObjectPath("one"), JObjectPath("zwei"), JObjectPath("trois"), JArrayPath(4), JPathLink, JObjectPath("cinque"))) {
        JPath.fromString("one[zwei].trois[4]>.cinque")
      }
    assertResult(
      JPath(JObjectPath("one"), JObjectPath("zwei"), JObjectPath("trois"), JDefaultValue("abc"), JArrayPath(4), JPathLink, JObjectPath("cinque"))) {
        JPath.fromString("one[zwei].trois(abc)[4]>.cinque")
      }
  }
  
  it should "be able to interpret string format syntax" in {
    assertResult(
      JPath(JObjectPath("one"), JArrayPath(2), JObjectPath("trois"), 
          JStringFormat(
              Seq(FormatLiteral("qu"), ReplaceHolder, FormatLiteral("arto"), ReplaceHolder, ReplaceHolder, FormatLiteral("five")),
              Seq(JPath(JObjectPath("vier"), JObjectPath("funf"), JPathLink, JObjectPath("id")),
                  JPath(JObjectPath("quatro")),
                  JPath(JObjectPath("four"), JArrayPath(4)))))) {
        JPath.fromString("one[2].trois|qu${vier.funf>.id}arto${quatro}${four[4]}five")
      }
  }
  
  it should "be able to read blank string as blank path" in {
    assertResult(JPath()) {
      JPath.fromString("")
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
  
  it should "allow expressions starting with array access via brackets" in {
    assertResult(true) {
      JPath.validate("[1].zwei.trois")
    }
    assertResult(true) {
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
  
  it should "validate string format expressions" in {
    assert(
     JPath.validate("one[zwei].trois|qu${vier.funf>.id}arto${quatro}${four[4]}five")   
    )
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
  
  "JPath StringFormatExpression patterns" should "match with corresponding string expressions" in {
    val strMatches = Seq(
        """|key${key.key}key${key}key""",
        """|${key.key}key${key}key""",
        """|${key[key](key)}${key}key""",
        """|${key.key}""",
        """|key${key[key]>.key}""",
        """|key${key.key}key${key}""",
        """|key${key.key>.key}key${key}${key[key]}key""")
        
    val p = StringFormatExpression.patterns(0)
        
    assert(
      strMatches.forall { (str: String) => str match {
        case p(_*) => true
        case _ => {println(str);false}
      }}
    )
    
  }

}
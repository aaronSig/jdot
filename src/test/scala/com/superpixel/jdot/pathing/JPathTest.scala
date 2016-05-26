package com.superpixel.jdot.pathing

import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.FlatSpec

import com.superpixel.jdot.pathing.JPath.StringFormatExpression;


class JPathTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  "JPath fromString" should "be able to interpret dot syntax" in {

    assertResult(
      JPath(JObjectPath("one"), JObjectPath("zwei"), JObjectPath("trois"))) {
        JPath.fromString("one.zwei.trois")
      }
  }
  
  it should "be able to interpret dot syntax with spaces in json key" in {

    assertResult(
      JPath(JObjectPath("one key"), JObjectPath("zwei key"), JObjectPath("trois key"))) {
        JPath.fromString("one key.zwei key.trois key")
      }
  }
  
  it should "be able to interpred escaped json keys" in {
    assertResult(
      JPath(JObjectPath("on.e"), JObjectPath("zw>ei"), JObjectPath("tr[ois"))) {
        JPath.fromString("""on\.e.zw\>ei.tr\[ois""")
      }
  }
  
  it should "be able to interpret a nested path as a json key" in {
    assertResult(
      JPath(JObjectPath("one"), JObjectPathFromValue(
        JPath(JObjectPath("ein"), JObjectPath("zwei"), JObjectPath("drei"))  
      ), JObjectPath("trois"))) {
        JPath.fromString("one.{ein.zwei.drei}.trois")
      }
  }
  
  it should "be able to interpret a literal path as a json key" in {
    assertResult(
      JPath(JObjectPath("one"), JObjectPath("No. 2 in German Language"), JObjectPath("trois"))) {
        JPath.fromString("one.(No. 2 in German Language).trois")
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
  
  it should "be able to interpret a nested path as a array index" in {
    assertResult(
      JPath(JObjectPath("one"), JArrayPathFromValue(
        JPath(JObjectPath("ein"), JObjectPath("zwei"), JObjectPath("drei"))  
      ), JObjectPath("trois"))) {
        JPath.fromString("one[{ein.zwei.drei}].trois")
      }
  }
  
  it should "be able to interpret square brackets array syntax at start" in {
    assertResult(
      JPath(JArrayPath(1), JObjectPath("zwei"), JObjectPath("trois"))) {
        JPath.fromString("[1].zwei[trois]")
      }
  }
  
  it should "be able to interpret literal expression" in {
    assertResult(JPath(JPathValue("one", None))) {
      JPath.fromString("(one)")
    }
  }
  
  it should "be able to interpret literal expression with transmute" in {
    assertResult(JPath(JPathValue("1", None), JTransmute("i", None))) {
      JPath.fromString("(1)^i")
    }
  }
  
  it should "be able to interpret an embedded transmute expression" in {
    assertResult(JPath(JPathValue("1", Some(JTransmute("i", None))))) {
      JPath.fromString("(1^i)")
    }
    assertResult(JPath(JPathValue("true", Some(JTransmute("b", Some("!")))))) {
      JPath.fromString("(true^b<!)")
    }
  }
  
  it should "be able to interpret blank literal expression with transmute" in {
    assertResult(JPath(JPathValue("", None))) {
      JPath.fromString("()")
    }
  }
  
  it should "be able to interpret default values" in {
    assertResult(
        JPath(JObjectPath("one"), JObjectPath("zwei"), JDefaultValue("abc", None))) {
      JPath.fromString("one.zwei(abc)")
    }
    assertResult(
        JPath(JObjectPath("one"), JDefaultValue("abc", None), JObjectPath("zwei"))) {
      JPath.fromString("one(abc).zwei")
    }
  }
  
  it should "be able to interpret blank default values" in {
    assertResult(
        JPath(JObjectPath("one"), JObjectPath("zwei"), JDefaultValue("", None))) {
      JPath.fromString("one.zwei()")
    }
    assertResult(
        JPath(JObjectPath("one"), JDefaultValue("", None), JObjectPath("zwei"))) {
      JPath.fromString("one().zwei")
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
      JPath(JObjectPath("one"), JObjectPath("zwei"), JObjectPath("trois"), JDefaultValue("abc", None), JArrayPath(4), JPathLink, JObjectPath("cinque"))) {
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
        JPath.fromString("one[2].trois|qu{vier.funf>.id}arto{quatro}{four[4]}five")
      }
  }
  
  
  it should "be able to interpret string format syntax, treating select parts as literals" in {
    assertResult(
      JPath(JObjectPath("one"), JObjectPath("two"), 
          JStringFormat(
              Seq(FormatLiteral("web.start?id="), ReplaceHolder),
              Seq(JPath(JObjectPath("three"), JDefaultValue("1.234", Some(JTransmute("n", None)))))))) {
        JPath.fromString("one.two|web.start?id={three(1.234^n)}")
      }
  }
  
  it should "be able to interpret triky string format syntax, where literal is a delim" in {
    assertResult(
      JPath(JObjectPath("one"), JObjectPath("two"), 
          JStringFormat(
              Seq(FormatLiteral("comp="), ReplaceHolder, FormatLiteral("."), ReplaceHolder),
              Seq(JPath(JObjectPath("three")),
                  JPath(JObjectPath("four")))))) {
        JPath.fromString("one.two|comp={three}.{four}")
      }
  }
  
  it should "be able to interpret triky string format syntax, where literal is a delim and with clarifying brackets" in {
    assertResult(
      JPath(JObjectPath("one"), JObjectPath("two"), 
          JStringFormat(
              Seq(FormatLiteral("comp="), ReplaceHolder, FormatLiteral("."), ReplaceHolder),
              Seq(JPath(JObjectPath("three")),
                  JPath(JObjectPath("four")))))) {
        JPath.fromString("one.two|(comp={three}.{four})")
      }
  }
  
  it should "be able to interpret conditional syntax" in {
    assertResult(
      JPath(JObjectPath("one"), JArrayPath(2), JConditional(
        JPath(JObjectPath("three"), JObjectPath("four"), JPathLink, JObjectPath("five")), None,
        JPath(JObjectPath("three"), JObjectPath("four"), JPathLink, JObjectPath("five"), JObjectPath("six")),
        JPath(JObjectPath("notThree"))
      ))) {
      JPath.fromString("one[2]~three.four>.five?three.four>.five.six:notThree")
    }
  }
  
  it should "be able to interpret conditional syntax with clarifying brackets" in {
    assertResult(
      JPath(JObjectPath("one"), JArrayPath(2), JConditional(
        JPath(JObjectPath("three"), JObjectPath("four"), JPathLink, JObjectPath("five")), None,
        JPath(JObjectPath("three"), JObjectPath("four"), JPathLink, JObjectPath("five"), JObjectPath("six")),
        JPath(JObjectPath("notThree"))
      ))) {
      JPath.fromString("one[2]~{{three.four>.five}?{three.four>.five.six}:notThree}")
    }
  }
  
  it should "be able to read blank string as blank path" in {
    assertResult(JPath()) {
      JPath.fromString("")
    }
  }
  
  it should "be able to interpret transmutation expressions" in {
    assertResult(JPath(JObjectPath("one"), JTransmute("n", None))) {
      JPath.fromString("one^n")
    }
  }
  
  it should "be able to interpret transmutation expressions with argument" in {
    assertResult(JPath(JObjectPath("one"), JTransmute("f", Some("2.2")))) {
      JPath.fromString("one^f<2.2")
    }
  }
  
  it should "be able to interpret tricky 1" in {
    assertResult(
      JPath(JObjectPath("one"), JConditional(
            JPath(JObjectPath("two")),
            Some(JPath(JObjectPath("three"))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral(" equals "), ReplaceHolder),
                Seq(JPath(JObjectPath("two"), JDefaultValue("2", Some(JTransmute("n", None)))),
                    JPath(JObjectPath("three"), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath("three"), JDefaultValue("3", Some(JTransmute("n", None)))),
                    JPath(JObjectPath("two"), JDefaultValue("2", None)))
            ))), JTransmute("s", None)
      )    
    ){
     JPath.fromString("one~{two=three?|{two(2^n)} equals {three(3)}:|({three(3^n)}ne{two(2)})}^s")
    }
  }
  
  it should "be able to interpret tricky 2" in {
    assertResult(
      JPath(JObjectPath("one"), JConditional(
            JPath(JObjectPath("two")),
            Some(JPath(JObjectPath("three"))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral(" equals "), ReplaceHolder),
                Seq(JPath(JObjectPath("two"), JDefaultValue("2", Some(JTransmute("f", Some(".2"))))),
                    JPath(JObjectPath("three"), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath("three"), JDefaultValue("3", Some(JTransmute("f", Some(".2"))))),
                    JPath(JObjectPath("two"), JDefaultValue("2", None)))
            ))), JTransmute("s", None)
      )    
    ){
     JPath.fromString("one~{two=three?|{two(2^f<(.2))} equals {three(3)}:|{three(3^f<(.2))}ne{two(2)}}^s")
    }
  }
  
  it should "be able to interpret tricky 3" in {
    
     assertResult(
      JPath(JObjectPath("one"), JConditional(
            JPath(JObjectPath("two")),
            Some(JPath(JObjectPath("three"))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral(" equals "), ReplaceHolder),
                Seq(JPath(JObjectPath("two"), JDefaultValue("2", None)),
                    JPath(JObjectPath("three"), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath("three"), JDefaultValue("3", None)),
                    JPath(JObjectPath("two"), JDefaultValue("2", None)))
            )))
      )    
    ){
     JPath.fromString("one~two=three?|{two(2)} equals {three(3)}:|{three(3)}ne{two(2)}")
    }
  }
  
  it should "be able to interpret tricky 4" in {
    assertResult(
      JPath(JObjectPath("one"), JConditional(
            JPath(JObjectPath("two")),
            Some(JPath(JObjectPath("three"))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral(" :equals: "), ReplaceHolder),
                Seq(JPath(JObjectPath("two"), JDefaultValue("2", None)),
                    JPath(JObjectPath("three"), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath("three"), JDefaultValue("3", None)),
                    JPath(JObjectPath("two"), JDefaultValue("2", None)))
            )))
      )    
    ){
     JPath.fromString("one~two=three?|({two(2)}( :equals: ){three(3)}):|{three(3)}ne{two(2)}")
    }
  }
  
  it should "be able to interpret tricky 4.5" in {
    assertResult(
      JPath(JObjectPath("one"), JConditional(
            JPath(JObjectPath("two")),
            Some(JPath(JObjectPath("three"))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("( :equals: )"), ReplaceHolder),
                Seq(JPath(JObjectPath("two"), JDefaultValue("2", None)),
                    JPath(JObjectPath("three"), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath("three"), JDefaultValue("3", None)),
                    JPath(JObjectPath("two"), JDefaultValue("2", None)))
            )))
      )    
    ){
     JPath.fromString("""one~two=three?|({two(2)}\( :equals: \){three(3)}):|{three(3)}ne{two(2)}""")
    }
  }
  
  it should "be able to interpret tricky 4.75" in {
    assertResult(
      JPath(JObjectPath("one"), JConditional(
            JPath(JObjectPath("two")),
            Some(JPath(JObjectPath("three"))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("( :equals: )"), ReplaceHolder),
                Seq(JPath(JObjectPath("two"), JDefaultValue("2", None)),
                    JPath(JObjectPath("three"), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath("three"), JDefaultValue("3", None)),
                    JPath(JObjectPath("two"), JDefaultValue("2", None)))
            )))
      )    
    ){
     JPath.fromString("""one~two=three?|({two(2)}(\( :equals: \)){three(3)}):|{three(3)}ne{two(2)}""")
    }
  }
  
  it should "be able to interpret tricky 5" in {
    assertResult(
      JPath(JObjectPath("one"), JStringFormat(
                Seq(FormatLiteral("23"), ReplaceHolder, FormatLiteral("56")),
                Seq(JPath(JObjectPath("two")))
            ), JTransmute("f", Some("2.2"))
      )    
    ){
     JPath.fromString("one|23{two}56^f<2.2")
    }
  }
  
  it should "be able to interpret tricky 6" in {
    assertResult(
      JPath(JObjectPath("one"), JStringFormat(
                Seq(FormatLiteral("23"), ReplaceHolder, FormatLiteral("56")),
                Seq(JPath(JObjectPath("two")))
            ), JTransmute("f", Some("2.2"))
      )    
    ){
     JPath.fromString("one|(23{two}56)^f<(2.2)")
    }
  }
  
  it should "be able to interpret tricky 7" in {
    assertResult(
      JPath(JObjectPath("one"), JStringFormat(
                Seq(FormatLiteral("23"), ReplaceHolder, FormatLiteral("51")),
                Seq(JPath(JObjectPath("two")))
            ), JTransmute("ord", None)
      )    
    ){
     JPath.fromString("one|(23{two}51)^ord")
    }
  }
  
  it should "be able to interpret tricky 8" in {
    assertResult(
      JPath(JObjectPath("one"), JStringFormat(
                Seq(FormatLiteral("23"), ReplaceHolder, FormatLiteral("51")),
                Seq(JPath(JObjectPath("two")))
            ), JTransmute("%", None)
      )    
    ){
     JPath.fromString("one|(23{two}51)^%")
    }
  }
  
  it should "be able to interpret tricky 9" in {
    assertResult(
      JPath(JStringFormat(
                Seq(FormatLiteral("Hey "), ReplaceHolder, FormatLiteral("!"), ReplaceHolder),
                Seq(JPath(JObjectPath("name")), 
                    JPath(JStringFormat(
                        Seq(FormatLiteral("This is a hat: ^")), Seq())
                    , JTransmute("s", Some("U")))))
            , JTransmute("s", Some("l"))
      )    
    ){
     JPath.fromString("|(Hey {name}!{|(This is a hat: (^))^s<U})^s<l")
    }
  }
  
  it should "be able to interpret tricky 10" in {
    assertResult(
      JPath(JStringFormat(
                Seq(FormatLiteral("Hey "), ReplaceHolder, FormatLiteral("!"), ReplaceHolder),
                Seq(JPath(JObjectPath("name")), 
                    JPath(JStringFormat(
                        Seq(FormatLiteral("This is a hat: (^)")), Seq())
                    , JTransmute("s", Some("U")))))
            , JTransmute("s", Some("l"))
      )    
    ){
     JPath.fromString("""|(Hey {name}!{|(This is a hat: \(\^\))^s<U})^s<l""")
    }
  }

  "JPath validate" should "validate dot syntax" in {
    assertResult(true) {
      JPath.validate("one.zwei.trois")._1
    }
  }

  it should "validate and flag bad dot syntax (double dot)" in {
    assertResult(false) {
      JPath.validate("one..zwei.trois")._1
    }
  }

  it should "validate and flag bad dot syntax (leading dot)" in {
    assertResult(false) {
      JPath.validate(".one.zwei.trois")._1
    }
  }

  it should "validate and flag bad dot syntax (trailing dot)" in {
    assertResult(false) {
      JPath.validate("one.zwei.trois.")._1
    }
  }

  it should "validate bracket syntax" in {
    assertResult(true) {
      JPath.validate("one[zwei][trois]")._1
    }
  }
  
  it should "allow expressions starting with array access via brackets" in {
    assertResult(true) {
      JPath.validate("[1].zwei.trois")._1
    }
    assertResult(true) {
      JPath.validate("[one][zwei][trois]")._1
    }
  }

  it should "validate and flag bad bracket syntax (unopened brackets)" in {
    assertResult(false) {
      JPath.validate("one][zwei][trois]")._1
    }
  }

  it should "validate and flag bad bracket syntax (unclosed brackets)" in {
    assertResult(false) {
      JPath.validate("one[zwei][[trois]")._1
    }
  }

  it should "validate and flag bad bracket syntax (nested brackets)" in {
    assertResult(false) {
      JPath.validate("one[zwei][trois[quatro]]")._1
    }
  }

  it should "validate and flag bad bracket syntax (trailing brackets)" in {
    assertResult(false) {
      JPath.validate("one[zwei][trois][quatro][")._1
    }
  }

  it should "validate mixed syntax" in {
    assertResult(true) {
      JPath.validate("one.zwei[trois][quatro]")._1
    }
    assertResult(true) {
      JPath.validate("one[zwei].trois[quatro]")._1
    }
  }

  it should "validate and flag bad mixed syntax (nested dot)" in {
    assertResult(false) {
      JPath.validate("one[zwei][trois.quatro]")._1
    }
  }

  it should "validate and flag bad mixed syntax (dot to bracket)" in {
    assertResult(false) {
      JPath.validate("one[zwei].trois.[quatro]")._1
    }
  }
  
  it should "validate string format expressions" in {
    assert(
     JPath.validate("one[zwei].trois|qu{vier.funf>.id}arto{quatro}{four[4]}five")._1
    )
  }
  
  it should "validate tricky 1" in {
    assert(
     JPath.validate("one~{two=three?|{two(2)} equals {three(3)}:|({three(3)}ne{two(2)})}^s")._1
    )
  }
  
  it should "validate tricky 2" in {
    assert(
     JPath.validate("one~{two=three?|{two(2)} equals {three(3)}:|{three(3)}ne{two(2)}}^s")._1
    )
  }
  
  it should "validate tricky 3" in {
    assert(
     JPath.validate("one~two=three?|{two(2)} equals {three(3)}:|{three(3)}ne{two(2)}")._1
    )
  }
  
  it should "validate tricky 4" in {
    assert(
     JPath.validate("one~two=three?|({two(2)}( :equals: ){three(3)}):|{three(3)}ne{two(2)}")._1
    )
  }
  
  it should "validate tricky 5" in {
    assert(
     JPath.validate("one|23{two}56^f<2.2")._1
    )
  }
  
  it should "validate tricky 6" in {
    assert(
     JPath.validate("one|(23{two}56)^f<(2.2)")._1
    )
  }
  
  it should "validate tricky 7" in {
    assert(
     JPath.validate("one|(23{two}51)^ord")._1
    )
  }
  
  "JPath unescapeJsonKey" should "remove escaping from dots" in {
    assertResult("on.e") {
      JPath.unescapeJsonKey("""on\.e""")
    }
  }
  
  it should "remove escaping from square brackets" in {
    assertResult("o[n]e") {
      JPath.unescapeJsonKey("""o\[n\]e""")
    }
  }
  
  it should "remove escaping from chevron" in {
    assertResult("on>>e>") {
      JPath.unescapeJsonKey("""on\>\>e\>""")
    }
  }
  
  it should "remove escaping with multiple backslashes" in {
    assertResult("""on\\>.e\\\\]""") {
      JPath.unescapeJsonKey("""on\\\>\.e\\\\\]""")
    }
  }
  
  "JPath escapeJsonKey" should "escape dots" in {
    assertResult("""on\.e""") {
      JPath.escapeJsonKey("""on.e""")
    }
  }
  
  it should "escape square brackets" in {
    assertResult("""on\]\[e""") {
      JPath.escapeJsonKey("""on][e""")
    }
  }
  
  it should "escape chevron" in {
    assertResult("""on\>e""") {
      JPath.escapeJsonKey("""on>e""")
    }
  }
  
  it should "escape cancelled escaped special chars" in {
    assertResult("""on\\\.e""") {
      JPath.escapeJsonKey("""on\\.e""")
    }
  }
  
  "JPath escapeJsonKey and unescapeJsonKey" should "be inverse functions" in {
    val tricky = """>o\\"\\\\.n\\>e.tw\\o't\\]\\[h[ree""";
    assertResult(tricky) {
      JPath.unescapeJsonKey(JPath.escapeJsonKey(tricky))
    }
  }
  
  "JPath StringFormatExpression patterns" should "match with corresponding string expressions" in {
    import com.superpixel.jdot.pathing.JPath.StringFormatExpression;
    
    val lit = StringFormatExpression.literalStr
    val nest = StringFormatExpression.nestedPathStr
    val strMatches = Seq(
        """|lit{nest}lit{nest}lit""",
        """|{nest}lit{nest}lit""",
        """|{nest}{nest}lit""",
        """|{nest}""",
        """|lit{nest}""",
        """|lit{nest}lit{nest}""")
        
    val p = StringFormatExpression.patterns(0)
    assert(
      strMatches.forall { (str: String) => 
        str.replace("lit", lit).replace("nest", nest) match {
          case p(_*) => true
          case _ => {println(str);false}
        }
      }
    )
    
  }

}
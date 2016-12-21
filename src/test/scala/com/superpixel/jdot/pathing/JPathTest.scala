package com.superpixel.jdot.pathing

import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.FlatSpec

import com.superpixel.jdot.pathing.JPath.StringFormatExpression;


class JPathTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  "JPath fromString" should "be able to interpret dot syntax" in {

    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("zwei")), JObjectPath(LiteralKey("trois")))) {
        JPath.fromString("one.zwei.trois")
      }
  }
  
  it should "be able to interpret dot syntax with spaces in json key" in {

    assertResult(
      JPath(JObjectPath(LiteralKey("one key")), JObjectPath(LiteralKey("zwei key")), JObjectPath(LiteralKey("trois key")))) {
        JPath.fromString("one key.zwei key.trois key")
      }
  }
  
  it should "be able to interpred escaped json keys" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("on.e")), JObjectPath(LiteralKey("zw>ei")), JObjectPath(LiteralKey("tr[ois")))) {
        JPath.fromString("""on\.e.zw\>ei.tr\[ois""")
      }
  }
  
  it should "be able to interpret a nested path as a json key" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(KeyFromPath(
        JPath(JObjectPath(LiteralKey("ein")), JObjectPath(LiteralKey("zwei")), JObjectPath(LiteralKey("drei"))))  
      ), JObjectPath(LiteralKey("trois")))) {
        JPath.fromString("one.{ein.zwei.drei}.trois")
      }
  }
  
  it should "be able to interpret a literal path as a json key" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("No. 2 in German Language")), JObjectPath(LiteralKey("trois")))) {
        JPath.fromString("one.(No. 2 in German Language).trois")
      }
  }
  
  it should "be able to interpret square bracket syntax" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("zwei")), JObjectPath(LiteralKey("trois")))) {
        JPath.fromString("one[zwei][trois]")
      }
  }
  
  it should "be able to interpret square bracket array syntax" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JArrayPath(LiteralIndex(2)), JArrayPath(LiteralIndex(3)))) {
        JPath.fromString("one[2][3]")
      }
  }
  
  it should "be able to interpret a nested path as a array index" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JArrayPath(IndexFromPath(
        JPath(JObjectPath(LiteralKey("ein")), JObjectPath(LiteralKey("zwei")), JObjectPath(LiteralKey("drei"))))
      ), JObjectPath(LiteralKey("trois")))) {
        JPath.fromString("one[{ein.zwei.drei}].trois")
      }
  }
  
  it should "be able to interpret square brackets array syntax at start" in {
    assertResult(
      JPath(JArrayPath(LiteralIndex(1)), JObjectPath(LiteralKey("zwei")), JObjectPath(LiteralKey("trois")))) {
        JPath.fromString("[1].zwei[trois]")
      }
  }

  it should "be able to understand SelfReference meta key at start" in {
    assertResult(
      JPath(JMetaPath(SelfReferenceKey))) {
      JPath.fromString(SelfReferenceKey.str)
    }
  }

  it should "be able to understand SelfReference meta key within a path" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JMetaPath(SelfReferenceKey))) {
      JPath.fromString("one." + SelfReferenceKey.str)
    }
  }

  it should "be should not parse SelfReference is written within literal brackets, and instead read it as a object access key" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey(SelfReferenceKey.str)))) {
      JPath.fromString("one.(" + SelfReferenceKey.str + ")")
    }
  }

  it should "be able to understand NothingReference meta key at start" in {
    assertResult(
      JPath(JMetaPath(NothingReferenceKey))) {
      JPath.fromString(NothingReferenceKey.str)
    }
  }

  it should "be able to understand NothingReference meta key within a path" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JMetaPath(NothingReferenceKey))) {
      JPath.fromString("one." + NothingReferenceKey.str)
    }
  }

  it should "be should not parse NothingReference is written within literal brackets, and instead read it as a object access key" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey(NothingReferenceKey.str)))) {
      JPath.fromString("one.(" + NothingReferenceKey.str + ")")
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
    assertResult(JPath(JPathValue("true", Some(JTransmute("b", Some(LiteralArgument("!"))))))) {
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
        JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("zwei")), JDefaultValue("abc", None))) {
      JPath.fromString("one.zwei(abc)")
    }
    assertResult(
        JPath(JObjectPath(LiteralKey("one")), JDefaultValue("abc", None), JObjectPath(LiteralKey("zwei")))) {
      JPath.fromString("one(abc).zwei")
    }
  }
  
  it should "be able to interpret blank default values" in {
    assertResult(
        JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("zwei")), JDefaultValue("", None))) {
      JPath.fromString("one.zwei()")
    }
    assertResult(
        JPath(JObjectPath(LiteralKey("one")), JDefaultValue("", None), JObjectPath(LiteralKey("zwei")))) {
      JPath.fromString("one().zwei")
    }
  }
  
  it should "be able to interpret a mixture syntax" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("zwei")), JArrayPath(LiteralIndex(3)), JObjectPath(LiteralKey("quatro")))) {
        JPath.fromString("one.zwei[3][quatro]")
      }
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("zwei")), JObjectPath(LiteralKey("trois")), JArrayPath(LiteralIndex(4)), JPathLink, JObjectPath(LiteralKey("cinque")))) {
        JPath.fromString("one[zwei].trois[4]>.cinque")
      }
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("zwei")), JObjectPath(LiteralKey("trois")), JDefaultValue("abc", None), JArrayPath(LiteralIndex(4)), JPathLink, JObjectPath(LiteralKey("cinque")))) {
        JPath.fromString("one[zwei].trois(abc)[4]>.cinque")
      }
  }
  
  it should "be able to interpret string format syntax" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JArrayPath(LiteralIndex(2)), JObjectPath(LiteralKey("trois")), 
          JStringFormat(
              Seq(FormatLiteral("qu"), ReplaceHolder, FormatLiteral("arto"), ReplaceHolder, ReplaceHolder, FormatLiteral("five")),
              Seq(JPath(JObjectPath(LiteralKey("vier")), JObjectPath(LiteralKey("funf")), JPathLink, JObjectPath(LiteralKey("id"))),
                  JPath(JObjectPath(LiteralKey("quatro"))),
                  JPath(JObjectPath(LiteralKey("four")), JArrayPath(LiteralIndex(4))))))) {
        JPath.fromString("one[2].trois|qu{vier.funf>.id}arto{quatro}{four[4]}five")
      }
  }
  
  
  it should "be able to interpret string format syntax, treating select parts as literals" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("two")), 
          JStringFormat(
              Seq(FormatLiteral("web.start?id="), ReplaceHolder),
              Seq(JPath(JObjectPath(LiteralKey("three")), JDefaultValue("1.234", Some(JTransmute("n", None)))))))) {
        JPath.fromString("one.two|web.start?id={three(1.234^n)}")
      }
  }
  
  it should "be able to interpret triky string format syntax, where literal is a delim" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("two")), 
          JStringFormat(
              Seq(FormatLiteral("comp="), ReplaceHolder, FormatLiteral("."), ReplaceHolder),
              Seq(JPath(JObjectPath(LiteralKey("three"))),
                  JPath(JObjectPath(LiteralKey("four"))))))) {
        JPath.fromString("one.two|comp={three}.{four}")
      }
  }
  
  it should "be able to interpret triky string format syntax, where literal is a delim and with clarifying brackets" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("two")), 
          JStringFormat(
              Seq(FormatLiteral("comp="), ReplaceHolder, FormatLiteral("."), ReplaceHolder),
              Seq(JPath(JObjectPath(LiteralKey("three"))),
                  JPath(JObjectPath(LiteralKey("four"))))))) {
        JPath.fromString("one.two|(comp={three}.{four})")
      }
  }
  
  it should "be able to interpret conditional syntax" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JArrayPath(LiteralIndex(2)), JConditional(
        JPath(JObjectPath(LiteralKey("three")), JObjectPath(LiteralKey("four")), JPathLink, JObjectPath(LiteralKey("five"))), None,
        JPath(JObjectPath(LiteralKey("three")), JObjectPath(LiteralKey("four")), JPathLink, JObjectPath(LiteralKey("five")), JObjectPath(LiteralKey("six"))),
        JPath(JObjectPath(LiteralKey("notThree")))
      ))) {
      JPath.fromString("one[2]~three.four>.five?three.four>.five.six:notThree")
    }
  }
  
  it should "be able to interpret conditional syntax with clarifying brackets" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JArrayPath(LiteralIndex(2)), JConditional(
        JPath(JObjectPath(LiteralKey("three")), JObjectPath(LiteralKey("four")), JPathLink, JObjectPath(LiteralKey("five"))), None,
        JPath(JObjectPath(LiteralKey("three")), JObjectPath(LiteralKey("four")), JPathLink, JObjectPath(LiteralKey("five")), JObjectPath(LiteralKey("six"))),
        JPath(JObjectPath(LiteralKey("notThree")))
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
    assertResult(JPath(JObjectPath(LiteralKey("one")), JTransmute("n", None))) {
      JPath.fromString("one^n")
    }
  }
  
  it should "be able to interpret transmutation expressions with argument" in {
    assertResult(JPath(JObjectPath(LiteralKey("one")), JTransmute("f", Some(LiteralArgument("2.2"))))) {
      JPath.fromString("one^f<2.2")
    }
  }
  
  it should "be able to interpret transmutation expressions with nested path argument" in {
    assertResult(JPath(JObjectPath(LiteralKey("one")), JTransmute("f", Some(NestedArgument(
        JPath(JObjectPath(LiteralKey("one")), JObjectPath(LiteralKey("two")), JObjectPath(LiteralKey("three")))
      ))))) {
      JPath.fromString("one^f<{one.two.three}")
    }
  }
  
  it should "be able to interpret tricky 1" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JConditional(
            JPath(JObjectPath(LiteralKey("two"))),
            Some(JPath(JObjectPath(LiteralKey("three")))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral(" equals "), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", Some(JTransmute("n", None)))),
                    JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", Some(JTransmute("n", None)))),
                    JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)))
            ))), JTransmute("s", None)
      )    
    ){
     JPath.fromString("one~{two=three?|{two(2^n)} equals {three(3)}:|({three(3^n)}ne{two(2)})}^s")
    }
  }
  
  it should "be able to interpret tricky 2" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JConditional(
            JPath(JObjectPath(LiteralKey("two"))),
            Some(JPath(JObjectPath(LiteralKey("three")))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral(" equals "), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", Some(JTransmute("f", Some(LiteralArgument(".2")))))),
                    JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", Some(JTransmute("f", Some(LiteralArgument(".2")))))),
                    JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)))
            ))), JTransmute("s", None)
      )    
    ){
     JPath.fromString("one~{two=three?|{two(2^f<(.2))} equals {three(3)}:|{three(3^f<(.2))}ne{two(2)}}^s")
    }
  }
  
  it should "be able to interpret tricky 3" in {
    
     assertResult(
      JPath(JObjectPath(LiteralKey("one")), JConditional(
            JPath(JObjectPath(LiteralKey("two"))),
            Some(JPath(JObjectPath(LiteralKey("three")))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral(" equals "), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)),
                    JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)),
                    JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)))
            )))
      )    
    ){
     JPath.fromString("one~two=three?|{two(2)} equals {three(3)}:|{three(3)}ne{two(2)}")
    }
  }
  
  it should "be able to interpret tricky 4" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JConditional(
            JPath(JObjectPath(LiteralKey("two"))),
            Some(JPath(JObjectPath(LiteralKey("three")))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral(" :equals: "), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)),
                    JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)),
                    JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)))
            )))
      )    
    ){
     JPath.fromString("one~two=three?|({two(2)}( :equals: ){three(3)}):|{three(3)}ne{two(2)}")
    }
  }
  
  it should "be able to interpret tricky 4.5" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JConditional(
            JPath(JObjectPath(LiteralKey("two"))),
            Some(JPath(JObjectPath(LiteralKey("three")))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("( :equals: )"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)),
                    JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)),
                    JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)))
            )))
      )    
    ){
     JPath.fromString("""one~two=three?|({two(2)}\( :equals: \){three(3)}):|{three(3)}ne{two(2)}""")
    }
  }
  
  it should "be able to interpret tricky 4.75" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JConditional(
            JPath(JObjectPath(LiteralKey("two"))),
            Some(JPath(JObjectPath(LiteralKey("three")))),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("( :equals: )"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)),
                    JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)))
            )),
            JPath(JStringFormat(
                Seq(ReplaceHolder, FormatLiteral("ne"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("three")), JDefaultValue("3", None)),
                    JPath(JObjectPath(LiteralKey("two")), JDefaultValue("2", None)))
            )))
      )    
    ){
     JPath.fromString("""one~two=three?|({two(2)}(\( :equals: \)){three(3)}):|{three(3)}ne{two(2)}""")
    }
  }
  
  it should "be able to interpret tricky 5" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JStringFormat(
                Seq(FormatLiteral("23"), ReplaceHolder, FormatLiteral("56")),
                Seq(JPath(JObjectPath(LiteralKey("two"))))
            ), JTransmute("f", Some(LiteralArgument("2.2")))
      )    
    ){
     JPath.fromString("one|23{two}56^f<2.2")
    }
  }
  
  it should "be able to interpret tricky 6" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JStringFormat(
                Seq(FormatLiteral("23"), ReplaceHolder, FormatLiteral("56")),
                Seq(JPath(JObjectPath(LiteralKey("two"))))
            ), JTransmute("f", Some(LiteralArgument("2.2")))
      )    
    ){
     JPath.fromString("one|(23{two}56)^f<(2.2)")
    }
  }
  
  it should "be able to interpret tricky 7" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JStringFormat(
                Seq(FormatLiteral("23"), ReplaceHolder, FormatLiteral("51")),
                Seq(JPath(JObjectPath(LiteralKey("two"))))
            ), JTransmute("ord", None)
      )    
    ){
     JPath.fromString("one|(23{two}51)^ord")
    }
  }
  
  it should "be able to interpret tricky 8" in {
    assertResult(
      JPath(JObjectPath(LiteralKey("one")), JStringFormat(
                Seq(FormatLiteral("23"), ReplaceHolder, FormatLiteral("51")),
                Seq(JPath(JObjectPath(LiteralKey("two"))))
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
                Seq(JPath(JObjectPath(LiteralKey("name"))), 
                    JPath(JStringFormat(
                        Seq(FormatLiteral("This is a hat: ^")), Seq())
                    , JTransmute("s", Some(LiteralArgument("U"))))))
            , JTransmute("s", Some(LiteralArgument("l")))
      )    
    ){
     JPath.fromString("|(Hey {name}!{|(This is a hat: (^))^s<U})^s<l")
    }
  }
  
  it should "be able to interpret tricky 10" in {
    assertResult(
      JPath(JStringFormat(
                Seq(FormatLiteral("Hey "), ReplaceHolder, FormatLiteral("!"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("name"))), 
                    JPath(JStringFormat(
                        Seq(FormatLiteral("This is a hat: (^)")), Seq())
                    , JTransmute("s", Some(LiteralArgument("U"))))))
            , JTransmute("s", Some(LiteralArgument("l")))
      )    
    ){
     JPath.fromString("""|(Hey {name}!{|(This is a hat: \(\^\))^s<U})^s<l""")
    }
  }
  
  it should "be able to interpret tricky 11" in {
    assertResult(
      JPath(JStringFormat(
                Seq(FormatLiteral("Hey "), ReplaceHolder, FormatLiteral("!"), ReplaceHolder),
                Seq(JPath(JObjectPath(LiteralKey("name"))), 
                    JPath(JStringFormat(
                        Seq(FormatLiteral("This is a hat: ^")), Seq())
                    , JTransmute("s", Some(NestedArgument(JPath(JObjectPath(LiteralKey("argField")), JObjectPath(LiteralKey("arg")))))))))
            , JTransmute("s", Some(LiteralArgument("l")))
      )    
    ){
     JPath.fromString("|(Hey {name}!{|(This is a hat: (^))^s<{argField.arg}})^s<l")
    }
  }

  it should "error out in a timely manner" in {
    val thrown = intercept[JPathException] {
      JPath.fromString("(ter")
    }
    assert(thrown.getMessage.contains(")"))
    assert(thrown.getMessage.contains("Expected closing bracket"))
    assert(thrown.getMessage.contains("end of path"))
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
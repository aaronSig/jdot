package com.superpixel.advokit

import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.FlatSpec

class JsonPathTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  "JsonPath apply" should "be able to interpret dot syntax" in {

    assertResult(
      JsonObjectAccess("one", JsonObjectAccess("zwei", JsonValueAccess("trois", JsonTerminus)))) {
        JsonPath("one.zwei.trois")
      }
  }
  
  it should "be able to interpret square bracket syntax" in {
    assertResult(
      JsonObjectAccess("one", JsonObjectAccess("zwei", JsonValueAccess("trois", JsonTerminus)))) {
        JsonPath("one[zwei][trois]")
      }
  }
  
  it should "be able to interpret a mixture syntax" in {
    assertResult(
      JsonObjectAccess("one", JsonObjectAccess("zwei", JsonObjectAccess("trois", JsonValueAccess("quatro", JsonTerminus))))) {
        JsonPath("one.zwei[trois][quatro]")
      }
    assertResult(
      JsonObjectAccess("one", JsonObjectAccess("zwei", JsonObjectAccess("trois", JsonValueAccess("quatro", JsonLink(JsonValueAccess("cinque", JsonTerminus))))))) {
        JsonPath("one[zwei].trois[quatro]>.cinque")
      }
  }

  "JsonPath validate" should "validate dot syntax" in {
    assertResult(true) {
      JsonPath.validate("one.zwei.trois")
    }
  }

  it should "validate and flag bad dot syntax (double dot)" in {
    assertResult(false) {
      JsonPath.validate("one..zwei.trois")
    }
  }

  it should "validate and flag bad dot syntax (leading dot)" in {
    assertResult(false) {
      JsonPath.validate(".one.zwei.trois")
    }
  }

  it should "validate and flag bad dot syntax (trailing dot)" in {
    assertResult(false) {
      JsonPath.validate("one.zwei.trois.")
    }
  }

  it should "validate bracket syntax" in {
    assertResult(true) {
      JsonPath.validate("one[zwei][trois]")
    }
  }

  it should "validate and flag bad bracket syntax (leading bracket)" in {
    assertResult(false) {
      JsonPath.validate("[one][zwei][trois]")
    }
  }

  it should "validate and flag bad bracket syntax (unopened brackets)" in {
    assertResult(false) {
      JsonPath.validate("one][zwei][trois]")
    }
  }

  it should "validate and flag bad bracket syntax (unclosed brackets)" in {
    assertResult(false) {
      JsonPath.validate("one[zwei][[trois]")
    }
  }

  it should "validate and flag bad bracket syntax (nested brackets)" in {
    assertResult(false) {
      JsonPath.validate("one[zwei][trois[quatro]]")
    }
  }

  it should "validate and flag bad bracket syntax (trailing brackets)" in {
    assertResult(false) {
      JsonPath.validate("one[zwei][trois][quatro][")
    }
  }

  it should "validate mixed syntax" in {
    assertResult(true) {
      JsonPath.validate("one.zwei[trois][quatro]")
    }
    assertResult(true) {
      JsonPath.validate("one[zwei].trois[quatro]")
    }
  }

  it should "validate and flag bad mixed syntax (nested dot)" in {
    assertResult(false) {
      JsonPath.validate("one[zwei][trois.quatro]")
    }
  }

  it should "validate and flag bad mixed syntax (dot to bracket)" in {
    assertResult(false) {
      JsonPath.validate("one[zwei].trois.[quatro]")
    }
  }

}
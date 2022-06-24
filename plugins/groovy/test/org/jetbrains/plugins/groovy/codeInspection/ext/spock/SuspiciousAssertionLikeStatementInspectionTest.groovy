// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.groovy.codeInspection.ext.spock

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.testFramework.LightProjectDescriptor
import org.jetbrains.plugins.groovy.GroovyProjectDescriptors
import org.jetbrains.plugins.groovy.LightGroovyTestCase
import org.jetbrains.plugins.groovy.codeInspection.bugs.GrNamedVariantLabelsInspection
import org.jetbrains.plugins.groovy.ext.spock.SpockTestBase
import org.jetbrains.plugins.groovy.util.HighlightingTest
import org.junit.Test

class SuspiciousAssertionLikeStatementInspectionTest  extends SpockTestBase implements HighlightingTest{

  //final SuspiciousAssertionLikeStatementInspection inspection = new SuspiciousAssertionLikeStatementInspection()

  @Override
  Collection<Class<? extends LocalInspectionTool>> getInspections() {
    [SuspiciousAssertionLikeStatementInspection]
  }


  @Test
  void noProblem(){
    runTest('''
    expect:
    1 == 1
    ''')
  }

  @Test
  void detectsSimpleProblem(){
    runTest('''
    expect:
    if (true) {
      1 == 1
    }
    ''')
  }

  @Test
  void detectsComplexProblem(){
    runTest('''
    given:
    def a = 1
    
    when:
    def b = 2
    
    then:
    if (true) {
      a >= b
    }
    ''')
  }

  def runTest(String methodBody) {
    highlightingTest("""
class AClass extends spock.lang.Specification {
  def "a test"() {
    $methodBody
  }
}
""")
  }
}

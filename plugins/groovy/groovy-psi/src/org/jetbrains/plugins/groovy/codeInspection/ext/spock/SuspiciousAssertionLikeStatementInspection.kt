// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.groovy.codeInspection.ext.spock

import com.intellij.psi.util.parentOfType
import org.jetbrains.plugins.groovy.codeInspection.BaseInspection
import org.jetbrains.plugins.groovy.codeInspection.BaseInspectionVisitor
import org.jetbrains.plugins.groovy.ext.spock.SpockConstants.*
import org.jetbrains.plugins.groovy.ext.spock.SpockUtils.isSpecification
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrLabeledStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrBinaryExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod

class SuspiciousAssertionLikeStatementInspection : BaseInspection() {

  override fun buildVisitor(): BaseInspectionVisitor = SuspiciousAssertionLikeStatementInspectionVisitor()


  private class SuspiciousAssertionLikeStatementInspectionVisitor : BaseInspectionVisitor() {
    var insideThenOrExpectBlock = false
    var insideSpecialMethodCall = false
    var specialMethodCallExpression: GrMethodCallExpression? = null

    fun isInImplicitAssertionContext() = insideThenOrExpectBlock || insideSpecialMethodCall

    override fun visitStatement(statement: GrStatement) {
      println("visitStatement: ${statement.javaClass.name}")
      super.visitStatement(statement)
    }

    override fun visitExpression(expression: GrExpression) {
      println("visitExpression: ${expression.javaClass.name}")
      super.visitExpression(expression)
    }

    override fun visitElement(element: GroovyPsiElement) {
      println("visitElement: ${element.javaClass.name}")
      super.visitElement(element)
    }

    override fun visitClassDefinition(classDefinition: GrClassDefinition) {
      println("visitClassDefinition: $classDefinition")
      // we only care about Specification classes
      if (isSpecification(classDefinition)) {
        super.visitClassDefinition(classDefinition)
      }
    }

    override fun visitLabeledStatement(labeledStatement: GrLabeledStatement) {
      println("visitLabeledStatement: ${labeledStatement.name}")
      // and-labels just extend the previous block, so we ignore them
      if (labeledStatement.name == AND_LABEL) {
        return super.visitLabeledStatement(labeledStatement)
      }

      insideThenOrExpectBlock = (labeledStatement.name == THEN_LABEL || labeledStatement.name == EXPECT_LABEL)
      super.visitLabeledStatement(labeledStatement)
      //for (sibling in labeledStatement.siblings(true, false).skipTokens(TokenSets.WHITE_SPACES_OR_COMMENTS)) {
      //
      //}
    }

    override fun visitBinaryExpression(expression: GrBinaryExpression) {
      println("visitBinaryExpression: $expression")
      if (isInImplicitAssertionContext()) {
        if(!hasValidParent(expression)) {
          registerError(expression)
        }
      }
      else {
        super.visitBinaryExpression(expression)
      }
    }

    override fun visitMethodCallExpression(methodCallExpression: GrMethodCallExpression) {
      println("visitMethodCallExpression: $methodCallExpression")
      if (isSpecification(methodCallExpression.parentOfType()) &&
          (methodCallExpression.isWithOrVerifyAll() || methodCallExpression.isConditionBlock())) {
        val prevInsideSpecialMethodCall = insideSpecialMethodCall
        val prevSpecialMethodCallExpression = specialMethodCallExpression
        specialMethodCallExpression = methodCallExpression
        insideSpecialMethodCall = true
        super.visitMethodCallExpression(methodCallExpression)
        insideSpecialMethodCall = prevInsideSpecialMethodCall
        specialMethodCallExpression = prevSpecialMethodCallExpression
      }
      else {
        // TODO check for implicit assertion
        super.visitMethodCallExpression(methodCallExpression)
      }
    }

    fun hasValidParent(expression: GrExpression): Boolean {
      return expression.parentOfType<GrStatement>()?.let {
        when (it) {
          is GrMethod -> true
          is GrLabeledStatement -> it.name == THEN_LABEL || it.name == EXPECT_LABEL
          else -> false
        }
      } ?: return true
    }

    fun GrMethodCallExpression.isConditionBlock() = resolveMethod()?.annotations?.find { it.qualifiedName == CONDITION_BLOCK_ANNOTATION } != null
    fun GrMethodCallExpression.isWithOrVerifyAll() = isSpecification(
      resolveMethod()?.containingClass) && (name == WITH_METHOD_NAME || name == VERIFY_ALL_METHOD_NAME)
  }
}
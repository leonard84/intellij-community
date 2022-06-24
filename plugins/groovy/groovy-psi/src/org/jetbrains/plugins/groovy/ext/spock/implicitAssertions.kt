// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.groovy.ext.spock

import com.intellij.psi.PsiType
import com.intellij.psi.util.siblings
import com.intellij.psi.util.skipTokens
import org.jetbrains.plugins.groovy.ext.spock.SpockConstants.*
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrLabeledStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod

fun GrExpression.isImplicitAssertion(): Boolean {
  return false
}

fun GrExpression.isImplicitAssertionCandidate(): Boolean {
  if (this is GrMethodCallExpression) {
    return this.resolveMethod()?.returnType != PsiType.VOID // non-void methods are treated as assertions
  }
  return true
}

fun GrStatement.isTopLevelStatement(): Boolean {
  return (this.parent is GrOpenBlock && this.parent.parent is GrMethod) ||
         (this.parent is GrLabeledStatement && this.parent.parent is GrOpenBlock && this.parent.parent.parent is GrMethod)
}

private fun GrExpression.isInImplicitAssertionBlock(): Boolean {
  val parent = parent
  if (parent is GrLabeledStatement && parent.name == AND_LABEL) {
    return parent.isInImplicitAssertionBlock()
  }
  else {
    return (this as GrStatement).isInImplicitAssertionBlock()
  }
}

/**
 * @return `true` if some previous sibling is a then or expect block,
 *         otherwise `false`
 */
private fun GrStatement.isInImplicitAssertionBlock(): Boolean {
  for (sibling in siblings(false, false).skipTokens(TokenSets.WHITE_SPACES_OR_COMMENTS)) {
    if (sibling !is GrLabeledStatement) {
      continue
    }
    if (sibling.name == AND_LABEL) {
      continue
    }
    return findThenOrExpectLabeledStatement(sibling) != null
  }
  return false
}

private fun findThenOrExpectLabeledStatement(top: GrLabeledStatement): GrStatement? = findLabeledStatement(top) {
  it == THEN_LABEL || it == EXPECT_LABEL
}
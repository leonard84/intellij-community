// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.ext.spock

import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import com.intellij.psi.util.skipTokens
import org.jetbrains.plugins.groovy.ext.spock.SpockConstants.AND_LABEL
import org.jetbrains.plugins.groovy.ext.spock.SpockConstants.WHERE_LABEL
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets.WHITE_SPACES_OR_COMMENTS
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementTypes
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrLabeledStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrBinaryExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression

/**
 * @return `true` if [this] expression is a binary `|` or `||` expression which is a part of Spock data table,
 *         otherwise `false`
 */
fun GrExpression.isTableColumnSeparator(): Boolean {
  if (this !is GrBinaryExpression || !isOr()) return false
  val maybeTableRow = findMaybeTableRow() ?: return false
  return maybeTableRow.isUnderTableHeader() || maybeTableRow.isTableRow()
}

/**
 * Skips table columns (`|` or `||` expression) up.
 *
 * @return topmost `|` or `||` expression
 */
private fun GrBinaryExpression.findMaybeTableRow(): GrBinaryExpression? {
  var current = this
  while (true) {
    val parent = current.parent
    if (parent == null) {
      return null
    }
    else if (parent is GrBinaryExpression && parent.isOr()) {
      current = parent
    }
    else {
      return current
    }
  }
}

/**
 * @return `true` if [this] expression has `where:` label
 */
private fun PsiElement.isUnderTableHeader(): Boolean {
  return parent?.isTableHeader() ?: false
}

private fun PsiElement.isTableHeader(): Boolean {
  return this is GrLabeledStatement && name == WHERE_LABEL
}

private fun GrExpression.isTableRow(): Boolean {
  val parent = parent
  if (parent is GrLabeledStatement && parent.name == AND_LABEL) {
    return parent.isTableRow()
  }
  else {
    return (this as GrStatement).isTableRow()
  }
}

/**
 * @return `true` if some previous sibling is a table header and there are only table rows between them,
 *         otherwise `false`
 */
private fun GrStatement.isTableRow(): Boolean {
  for (sibling in siblings(false).drop(1).skipTokens(WHITE_SPACES_OR_COMMENTS)) {
    if (sibling.maybeTableColumnExpression()) {
      continue
    }
    if (sibling !is GrLabeledStatement) {
      return false
    }
    if (sibling.name == AND_LABEL && sibling.statement?.maybeTableColumnExpression() == true) {
      continue
    }
    return findWhereLabeledStatement(sibling) != null
  }
  return false
}

fun findWhereLabeledStatement(top: GrLabeledStatement): GrStatement? = findLabeledStatement(top) {
  it == WHERE_LABEL
}

private fun PsiElement.maybeTableColumnExpression() = this is GrBinaryExpression && isOr()

fun GrBinaryExpression.isOr(): Boolean {
  val type = operationTokenType
  return type === GroovyElementTypes.T_BOR || type === GroovyElementTypes.T_LOR
}

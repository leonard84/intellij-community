// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.groovy.ext.spock

import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrLabeledStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement


/**
 * foo:
 * bar:
 * where:
 * a << 1
 *
 * Such structure is parsed as `(foo: (bar: (where: (a << 1)))`.
 * We have to go deep and find `a << 1` statement.
 */
fun findLabeledStatement(top: GrLabeledStatement, labelNamePredicate: (String)->Boolean): GrStatement? {
  var current = top
  while (true) {
    val labeledStatement = current.statement
    when {
      labelNamePredicate(current.name) -> {
        return labeledStatement
      }
      labeledStatement is GrLabeledStatement -> {
        current = labeledStatement
      }
      else -> {
        return null
      }
    }
  }
}
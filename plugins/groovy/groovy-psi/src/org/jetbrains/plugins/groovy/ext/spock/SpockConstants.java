// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.ext.spock;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;

import java.util.Set;

public interface SpockConstants {

  @NlsSafe String SETUP_METHOD_NAME = "setup";
  @NlsSafe String CLEANUP_METHOD_NAME = "cleanup";
  @NlsSafe String SETUP_SPEC_METHOD_NAME = "setupSpec";
  @NlsSafe String CLEANUP_SPEC_METHOD_NAME = "cleanupSpec";
  @NlsSafe String WITH_METHOD_NAME = "with";
  @NlsSafe String VERIFY_ALL_METHOD_NAME = "verifyAll";

  @NlsSafe String WHERE_LABEL = "where";
  @NlsSafe String AND_LABEL = "and";
  @NlsSafe String EXPECT_LABEL = "expect";
  @NlsSafe String THEN_LABEL = "then";

  @NonNls String CONDITION_BLOCK_ANNOTATION = "org.spockframework.lang.ConditionBlock";
  Set<String> FIXTURE_METHOD_NAMES = ContainerUtil.immutableSet(
    SETUP_METHOD_NAME,
    CLEANUP_METHOD_NAME,
    SETUP_SPEC_METHOD_NAME,
    CLEANUP_SPEC_METHOD_NAME
  );

  Set<@NlsSafe String> FEATURE_METHOD_LABELS = Set.of(
    AND_LABEL, "setup", "given", EXPECT_LABEL, "when", THEN_LABEL, "cleanup", WHERE_LABEL
  );
}

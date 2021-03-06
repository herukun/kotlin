/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.inspections.coroutines

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import org.jetbrains.kotlin.idea.inspections.collections.AbstractCallChainChecker
import org.jetbrains.kotlin.idea.inspections.collections.SimplifyCallChainFix
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.psi.qualifiedExpressionVisitor
import org.jetbrains.kotlin.resolve.calls.model.DefaultValueArgument

class RedundantAsyncInspection : AbstractCallChainChecker() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        qualifiedExpressionVisitor(fun(expression) {
            var defaultContext: Boolean? = null
            var defaultStart: Boolean? = null
            var defaultParent: Boolean? = null
            val conversion = findQualifiedConversion(expression, conversionGroups) check@{ _, firstResolvedCall, _, _ ->
                for ((parameterDescriptor, valueArgument) in firstResolvedCall.valueArguments) {
                    val default = valueArgument is DefaultValueArgument
                    when (parameterDescriptor.name.asString()) {
                        "context" -> defaultContext = default
                        "start" -> defaultStart = default
                        "parent" -> defaultParent = default
                    }
                }
                true
            } ?: return
            defaultContext ?: return
            defaultStart ?: return
            if (defaultParent != true) return
            if (defaultContext!! && !defaultStart!!) return

            val replacement =
                if (defaultContext!! && defaultStart!!) "kotlinx.coroutines.experimental.runBlocking" else conversion.replacement
            val descriptor = holder.manager.createProblemDescriptor(
                expression,
                expression.firstCalleeExpression()!!.textRange.shiftRight(-expression.startOffset),
                "Redundant 'async' call may be reduced to '$replacement'",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
                SimplifyCallChainFix(replacement)
            )
            holder.registerProblem(descriptor)
        })

    private val conversionGroups = conversions.group()

    companion object {

        private val conversions = listOf(
            Conversion(
                "kotlinx.coroutines.experimental.async",
                "kotlinx.coroutines.experimental.Deferred.await",
                "kotlinx.coroutines.experimental.withContext"
            )
        )
    }
}
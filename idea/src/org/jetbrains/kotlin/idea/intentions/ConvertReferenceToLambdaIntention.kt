/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.intentions

import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.core.KotlinNameSuggester
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.inspections.IntentionBasedInspection
import org.jetbrains.kotlin.idea.util.IdeDescriptorRenderers
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.resolve.BindingContext.REFERENCE_TARGET
import org.jetbrains.kotlin.resolve.BindingContext.TYPE
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class ConvertReferenceToLambdaInspection : IntentionBasedInspection<KtCallableReferenceExpression>(ConvertReferenceToLambdaIntention::class)

class ConvertReferenceToLambdaIntention : SelfTargetingOffsetIndependentIntention<KtCallableReferenceExpression>(
        KtCallableReferenceExpression::class.java, "Convert reference to lambda"
) {
    val SOURCE_RENDERER = IdeDescriptorRenderers.SOURCE_CODE

    override fun applyTo(element: KtCallableReferenceExpression, editor: Editor?) {
        val context = element.analyze(BodyResolveMode.PARTIAL)
        val reference = element.callableReference
        val targetDescriptor = context[REFERENCE_TARGET, reference] as? CallableMemberDescriptor ?: return
        val parameterNamesAndTypes = targetDescriptor.valueParameters.map { it.name.asString() to it.type }
        val receiverTypeReference = element.typeReference
        val receiverType = receiverTypeReference?.let {
            context[TYPE, it] ?: return
        }
        val receiverNameAndType = receiverType?.let { KotlinNameSuggester.suggestNamesByType(it, validator = {
            name -> name !in parameterNamesAndTypes.map { it.first }
        }, defaultName = "receiver").first() to it }
        val acceptsReceiverAsParameter = receiverNameAndType != null &&
                                         (targetDescriptor.dispatchReceiverParameter != null ||
                                          targetDescriptor.extensionReceiverParameter != null)

        val referenceParent = element.parent
        val insideCall = referenceParent is KtValueArgument

        val factory = KtPsiFactory(element)
        val targetName = reference.text
        val lambdaParameterNamesAndTypes =
                if (acceptsReceiverAsParameter) listOf(receiverNameAndType!!) + parameterNamesAndTypes
                else parameterNamesAndTypes

        val receiverPrefix =
                if (acceptsReceiverAsParameter) receiverNameAndType!!.first + "."
                else receiverTypeReference?.let { it.text + "." } ?: ""
        val lambdaExpression = if (insideCall && lambdaParameterNamesAndTypes.size == 1) {
            factory.createLambdaExpression(
                    parameters = "",
                    body = when {
                        acceptsReceiverAsParameter ->
                            if (targetDescriptor is PropertyDescriptor) "it.$targetName"
                            else "it.$targetName()"
                        else ->
                            "$receiverPrefix$targetName(it)"
                    }
            )
        }
        else {
            factory.createLambdaExpression(
                    parameters = lambdaParameterNamesAndTypes.joinToString(separator = ", ") {
                        if (insideCall) it.first
                        else it.first + ": " + SOURCE_RENDERER.renderType(it.second)
                    },
                    body = if (targetDescriptor is PropertyDescriptor) {
                        "$receiverPrefix$targetName"
                    }
                    else {
                        parameterNamesAndTypes.joinToString(
                                prefix = "$receiverPrefix$targetName(",
                                separator = ", ",
                                postfix = ")"
                        ) { it.first }
                    }
            )
        }
        val lambdaResult = element.replace(lambdaExpression) as KtLambdaExpression
        ShortenReferences.DEFAULT.process(lambdaResult)

        if (insideCall) {
            val call = referenceParent?.parent?.parent as? KtCallExpression ?: return
            val moveOutOfParenthesis = MoveLambdaOutsideParenthesesIntention()
            if (moveOutOfParenthesis.isApplicableTo(call, referenceParent.startOffset)) {
                moveOutOfParenthesis.applyTo(call, editor)
            }
        }
    }

    override fun isApplicableTo(element: KtCallableReferenceExpression): Boolean {
        return true
    }
}
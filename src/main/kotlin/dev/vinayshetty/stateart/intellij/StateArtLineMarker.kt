package dev.vinayshetty.stateart.intellij

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.Function
import dev.vinayshetty.stateart.parser.StateMachineParser
import dev.vinayshetty.stateart.statemachinebuilder.StateMachineBuilderImpl
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtProperty
import java.awt.event.MouseEvent

class StateArtLineMarker : LineMarkerProvider {

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (isStateMachineIdentifier(psiElement)) {
            return LineMarkerInfo<PsiElement>(
                psiElement,
                psiElement.textRange,
                icon,
                Pass.LINE_MARKERS,
                Function { "State Art" },
                GutterIconNavigationHandler<PsiElement?> { mouseEvent: MouseEvent, psiElement: PsiElement? ->
                    writeDotFile(psiElement)
                },
                GutterIconRenderer.Alignment.CENTER
            )
        }
        return null
    }

    private fun writeDotFile(psiElement: PsiElement?) {
        psiElement?.let {
            val stateMachineText = psiElement.parent.text
            val stateMachineName = psiElement.text
            val stateMachineBuilder = StateMachineBuilderImpl()
            val stateMachineTokenizer = StateMachineParser(stateMachineBuilder)
        }
    }

    private fun isStateMachineIdentifier(psiElement: PsiElement): Boolean {
        return (psiElement as? LeafPsiElement)?.elementType == KtTokens.IDENTIFIER
                && psiElement.parent is KtProperty
                && psiElement.parent.text.matches(stateMachineRegex)
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<PsiElement>,
        result: MutableCollection<LineMarkerInfo<PsiElement>>
    ) {
    }

    companion object {
        private val icon = IconLoader.getIcon("/icons/stateart.png")
        private val stateMachineRegex = "(?s).*StateMachine[\\s]*.[\\s]*create\\<(?s).*".toRegex()
    }
}
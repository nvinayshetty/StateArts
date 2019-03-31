package dev.vinayshetty.stateart.intellij

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.Function
import dev.vinayshetty.stateart.dotsbuilder.DotsBuilderImpl
import dev.vinayshetty.stateart.lexer.StateMachineLexerImpl
import dev.vinayshetty.stateart.parser.StateMachineParser
import dev.vinayshetty.stateart.statemachinebuilder.StateMachineBuilderImpl
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtProperty
import java.awt.event.MouseEvent
import java.io.File
import java.nio.charset.Charset

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
            val project = psiElement.project
            val buildFolderPath = project.basePath + "/build"
            val stateMachineBuilder = StateMachineBuilderImpl()
            val stateMachineTokenizer = StateMachineParser(stateMachineBuilder)
            val stateMachineLexer = StateMachineLexerImpl(stateMachineTokenizer)
            val stateMachine = stateMachineLexer.lex(stateMachineText)
            val stateMachineDotString = DotsBuilderImpl().build(stateMachineName, stateMachine)
            val buildFolder = File(buildFolderPath)
            if (buildFolder.exists() && buildFolder.isDirectory) {
                val basepath = "$buildFolderPath/stateArt"
                val statesFolderPath = "$basepath/states"
                val artsFolderPath = "$basepath/arts"
                File(statesFolderPath).mkdirs()
                File(artsFolderPath).mkdirs()
                File("$statesFolderPath/$stateMachineName.dot").printWriter()
                    .use { file -> file.print(stateMachineDotString) }


                val generalCommandLine = GeneralCommandLine()
                generalCommandLine.charset = Charset.forName("UTF-8")
                generalCommandLine.setWorkDirectory(project.basePath)
                generalCommandLine.exePath = getExePath()
                generalCommandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.SYSTEM)
                generalCommandLine.withEnvironment(System.getenv())
                generalCommandLine.addParameter("dot")
                generalCommandLine.addParameter("-Tpng")
                generalCommandLine.addParameter("$statesFolderPath/$stateMachineName.dot")
                generalCommandLine.addParameter("-o")
                generalCommandLine.addParameter("$artsFolderPath/$stateMachineName.png")

                val processHandler = OSProcessHandler(generalCommandLine)
                processHandler.startNotify()

                LocalFileSystem.getInstance().refresh(true)
                val path =
                    LocalFileSystem.getInstance().refreshAndFindFileByPath("$artsFolderPath/$stateMachineName.png")
                path?.let {
                    FileEditorManager.getInstance(project).openFile(it, true, true)
                }
            }
        }
    }

    private fun getExePath(): String {
        val os = System.getProperty("os.name").toLowerCase()
        return if (os.contains("win")) {
            "c:/Program Files (x86)/Graphviz 2.28/bin/dot.exe"
        } else if (os.contains("mac")) {
            "/usr/local/bin/dot"
        } else {
            //consider Linux based
            "/usr/bin/dot"
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

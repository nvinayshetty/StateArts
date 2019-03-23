package dev.vinayshetty.stateart.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiElement

class StateArtLineMarker: LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<PsiElement>,
        result: MutableCollection<LineMarkerInfo<PsiElement>>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
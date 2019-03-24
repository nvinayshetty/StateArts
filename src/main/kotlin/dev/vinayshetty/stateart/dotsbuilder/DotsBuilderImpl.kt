package dev.vinayshetty.stateart.dotsbuilder

import dev.vinayshetty.stateart.datastructure.StateMachine

class DotsBuilderImpl : DotsBuilder {
    private var dotsString: String = "digraph"

    private fun addName(name: String): DotsBuilder = apply {
        dotsString += " $name {"
    }

    private fun addCurrentState(currentState: String): DotsBuilder = apply {
        dotsString += "\n $currentState -> "
    }

    private fun addEvent(update: String): DotsBuilder = apply {
        dotsString += " [label=\"$update\"]"
    }

    private fun addToState(toState: String): DotsBuilder = apply {
        dotsString += "$toState"
    }

    override fun build(name: String, stateMachine: StateMachine): String {
        addName(name)
        stateMachine.states.forEach {
            it.transitions.forEach { transition ->
                addCurrentState(it.name)
                addToState(transition.nextState)
                addEvent(transition.event.eventName)
            }
        }
        endDotFile()
        return dotsString
    }


    private fun endDotFile() {
        dotsString += "\n}"
    }

}
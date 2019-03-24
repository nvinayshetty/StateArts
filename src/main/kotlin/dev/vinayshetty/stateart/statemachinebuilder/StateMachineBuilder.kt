package dev.vinayshetty.stateart.statemachinebuilder

import dev.vinayshetty.stateart.datastructure.StateMachine

interface StateMachineBuilder {
    fun addState(name: String)
    fun addEvent(name: String)
    fun addTransitionToState(nextState: String)
    fun addNoTransition()
    fun build(): StateMachine
}
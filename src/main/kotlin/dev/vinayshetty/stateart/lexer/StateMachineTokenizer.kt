package dev.vinayshetty.stateart.lexer

import dev.vinayshetty.stateart.datastructure.StateMachine

interface StateMachineTokenizer {
    fun state(state: String)
    fun event(event: String)
    fun transition(nextState: String)
    fun doNotTransition()
    fun stateMachine(): StateMachine
}
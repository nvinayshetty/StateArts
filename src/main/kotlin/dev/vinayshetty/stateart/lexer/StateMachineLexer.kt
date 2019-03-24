package dev.vinayshetty.stateart.lexer

import dev.vinayshetty.stateart.datastructure.StateMachine

interface StateMachineLexer {
    fun lex(stateMachineString: String): StateMachine
}
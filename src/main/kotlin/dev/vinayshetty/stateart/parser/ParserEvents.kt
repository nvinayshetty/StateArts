package dev.vinayshetty.stateart.parser

sealed class ParserEvents {
    data class State(val name: String) : ParserEvents()
    data class Event(val event: String) : ParserEvents()
    data class Transition(val nextState: String) : ParserEvents()
    object NoTransition : ParserEvents()
}
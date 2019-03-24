package dev.vinayshetty.stateart.parser

import dev.vinayshetty.stateart.statemachinebuilder.StateMachineBuilder
import dev.vinayshetty.stateart.datastructure.StateMachine
import dev.vinayshetty.stateart.lexer.StateMachineTokenizer

class StateMachineParser(private val stateMachineBuilder: StateMachineBuilder) : StateMachineTokenizer {
    private var previousEvent: ParserEvents? = null

    override fun state(state: String) {
        handleParserEvent(ParserEvents.State(state))
    }

    override fun event(event: String) {
        handleParserEvent(ParserEvents.Event(event))
    }

    override fun transition(nextState: String) {
        handleParserEvent(ParserEvents.Transition(nextState))
    }

    override fun doNotTransition() {
        handleParserEvent(ParserEvents.NoTransition)
    }

    override fun stateMachine(): StateMachine {
        return stateMachineBuilder.build()
    }

    private fun handleParserEvent(currentEvent: ParserEvents) {
        if (previousEvent == null && currentEvent is ParserEvents.State) {
            stateMachineBuilder.addState(currentEvent.name)
        } else if (previousEvent != null) {
            when (currentEvent) {
                is ParserEvents.State -> {
                    when (previousEvent) {
                        is ParserEvents.Transition,
                        is ParserEvents.NoTransition -> stateMachineBuilder.addState(currentEvent.name)
                        else -> {
                            val message =
                                "Malformed StateMachine:Event/state ${currentEvent.name} can't appear after the  ${previousEvent.toString()}:"
                            throw StateMachineParserException(message)
                        }
                    }
                }
                is ParserEvents.Event -> {
                    when (previousEvent) {
                        is ParserEvents.State,
                        is ParserEvents.Transition,
                        is ParserEvents.NoTransition -> stateMachineBuilder.addEvent(currentEvent.event)
                        else -> {
                            val message =
                                """An eventName should't appear right after previous event,add transition for event ${currentEvent.event}
                                hint:${currentEvent.event} appears after $previousEvent
                            """
                            throw StateMachineParserException(message)
                        }
                    }
                }
                is ParserEvents.Transition -> {
                    when (previousEvent) {
                        is ParserEvents.Event -> stateMachineBuilder.addTransitionToState(currentEvent.nextState)
                        else -> {
                            val message =
                                "A transition can appear only after an event hint:$previousEvent appears after $currentEvent"
                            throw StateMachineParserException(message)
                        }
                    }
                }
                is ParserEvents.NoTransition -> {
                    when (previousEvent) {
                        is ParserEvents.Event -> stateMachineBuilder.addNoTransition()
                        else -> {
                            val message =
                                "doNotTransition can appear only after an event hint:dontTransition appears after ${previousEvent.toString()}"
                            throw StateMachineParserException(message)
                        }
                    }
                }
            }
        }
        previousEvent = currentEvent
    }
}
package dev.vinayshetty.stateart.builder

import dev.vinayshetty.stateart.datastructure.Event
import dev.vinayshetty.stateart.datastructure.State
import dev.vinayshetty.stateart.datastructure.StateMachine
import dev.vinayshetty.stateart.datastructure.Transition

class StateMachineBuilderImpl : StateMachineBuilder {
    private lateinit var currentState: State
    private lateinit var currentEvent: Event
    private val states = mutableSetOf<State>()
    private val transitions = mutableSetOf<Transition>()


    override fun addState(name: String) {
        if (::currentState.isInitialized) {
            val state = currentState.copy(transitions = transitions.toSet())
            states += state
            transitions.clear()
        }
        currentState = State(name, setOf())
    }

    override fun addEvent(name: String) {
        currentEvent = Event(name)
    }

    override fun addTransitionToState(nextState: String) {
        if (::currentEvent.isInitialized) {
            transitions += Transition(currentEvent, nextState)
        }
    }

    override fun addNoTransition() {
        if (::currentEvent.isInitialized) {
            transitions += Transition(currentEvent, currentState.name)
        }
    }

    override fun build(): StateMachine {
        return if (::currentState.isInitialized) {
            val state = currentState.copy(transitions = transitions)
            states += state
            return StateMachine(states)
        } else StateMachine(setOf())
    }
}
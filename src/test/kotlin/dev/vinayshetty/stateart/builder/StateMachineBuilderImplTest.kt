package dev.vinayshetty.stateart.builder

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StateMachineBuilderImplTest {

    private lateinit var stateMachineBuilder: StateMachineBuilder

    @BeforeEach
    fun setUp() {
        stateMachineBuilder = StateMachineBuilderImpl()
    }

    @Test
    fun `state machine with multiple States`() {
        stateMachineBuilder.addState("solid")
        stateMachineBuilder.addState("liquid")
        stateMachineBuilder.addState("gas")
        val stateMachine = stateMachineBuilder.build()
        assertThat(stateMachine.states.size, equalTo(3))
        assertThat(stateMachine.states.toList()[0].name, equalTo("solid"))
        assertThat(stateMachine.states.toList()[1].name, equalTo("liquid"))
        assertThat(stateMachine.states.toList()[2].name, equalTo("gas"))
    }

    @Test
    fun `empty State machine`() {
        val stateMachine = stateMachineBuilder.build()
        assertThat(stateMachine.states.size, equalTo(0))
    }

    @Test
    fun `events without transitions has no effect on state machine`() {
        stateMachineBuilder.addState("solid")
        stateMachineBuilder.addEvent("melt")
        stateMachineBuilder.addEvent("vaporize")
        stateMachineBuilder.addEvent("condense")
        val stateMachine = stateMachineBuilder.build()
        assertThat(stateMachine.states.size, equalTo(1))
        assertThat(stateMachine.states.toList()[0].name, equalTo("solid"))
        assertThat(stateMachine.states.toList()[0].transitions.size, equalTo(0))
    }

    @Test
    fun `adding transitions without current event has no effect on state machine`() {
        stateMachineBuilder.addTransitionToState("liquid")
        stateMachineBuilder.addTransitionToState("gas")
        val stateMachine = stateMachineBuilder.build()
        assertThat(stateMachine.states.size, equalTo(0))
    }

    @Test
    fun `Transitions are added to current state`() {
        stateMachineBuilder.addState("solid")

        stateMachineBuilder.addEvent("melt")
        stateMachineBuilder.addTransitionToState("liquid")

        stateMachineBuilder.addEvent("vaporize")
        stateMachineBuilder.addTransitionToState("gas")

        val stateMachine = stateMachineBuilder.build()

        assertThat(stateMachine.states.size, equalTo(1))
        assertThat(stateMachine.states.toList()[0].name, equalTo("solid"))
        val transitions = stateMachine.states.toList()[0].transitions.toList()
        assertThat(transitions.size, equalTo(2))
        assertThat(transitions[0].event.eventName, equalTo("melt"))
        assertThat(transitions[0].nextState, equalTo("liquid"))
        assertThat(transitions[1].event.eventName, equalTo("vaporize"))
        assertThat(transitions[1].nextState, equalTo("gas"))

    }

    @Test
    fun `addNoTransition is transition within same state`() {
        stateMachineBuilder.addState("solid")

        stateMachineBuilder.addEvent("freeze")
        stateMachineBuilder.addNoTransition()

        stateMachineBuilder.addEvent("cool down")
        stateMachineBuilder.addNoTransition()

        val stateMachine = stateMachineBuilder.build()

        assertThat(stateMachine.states.size, equalTo(1))
        assertThat(stateMachine.states.toList()[0].name, equalTo("solid"))
        val transitions = stateMachine.states.toList()[0].transitions.toList()
        assertThat(transitions.size, equalTo(2))
        assertThat(transitions[0].event.eventName, equalTo("freeze"))
        assertThat(transitions[0].nextState, equalTo("solid"))

        assertThat(transitions[1].event.eventName, equalTo("cool down"))
        assertThat(transitions[1].nextState, equalTo("solid"))
    }
}
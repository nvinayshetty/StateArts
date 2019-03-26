package dev.vinayshetty.stateart.parser

import dev.vinayshetty.stateart.statemachinebuilder.StateMachineBuilder
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations


internal class StateMachineParserTest {

    private lateinit var stateMachineParser: StateMachineParser

    @Mock
    private lateinit var stateMachineBuilder: StateMachineBuilder

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        stateMachineParser = StateMachineParser(stateMachineBuilder)
    }

    @Test
    fun `add initial state`() {
        stateMachineParser.state("liquid")
        verify(stateMachineBuilder).addState("liquid")
    }

    @Test
    fun `addState event after valid transitions`() {
        val orderVerifier = Mockito.inOrder(stateMachineBuilder)

        stateMachineParser.state("liquid")
        stateMachineParser.event("vaporize")
        stateMachineParser.transition("gas")
        stateMachineParser.state("gas")
        stateMachineParser.event("boil")
        stateMachineParser.doNotTransition()

        orderVerifier.verify(stateMachineBuilder).addState("liquid")
        orderVerifier.verify(stateMachineBuilder).addEvent("vaporize")
        orderVerifier.verify(stateMachineBuilder).addTransitionToState("gas")
        orderVerifier.verify(stateMachineBuilder).addState("gas")
        orderVerifier.verify(stateMachineBuilder).addEvent("boil")
        orderVerifier.verify(stateMachineBuilder).addNoTransition()
    }

    @Test
    fun `calling addState after an event should throw exception`() {
        val exception = assertThrows<StateMachineParserException> {
            stateMachineParser.state("liquid")
            stateMachineParser.event("vaporize")
            stateMachineParser.state("gas")
        }
        assertThat(
            exception.message,
            equalTo("Malformed StateMachine:Event/state gas can't appear after the  Event(event=vaporize):")
        )
    }

    @Test
    fun `state event  after previous State event should throw exception`() {
        val exception = assertThrows<StateMachineParserException> {
            stateMachineParser.state("liquid")
            stateMachineParser.state("gas")
        }
        assertThat(
            exception.message,
            CoreMatchers.equalTo("Malformed StateMachine:Event/state gas can't appear after the  State(name=liquid):")
        )
    }

    @Test
    fun `an Event can occur after previous  State, Transition or NoTransition`() {
        val orderVerifier = Mockito.inOrder(stateMachineBuilder)
        stateMachineParser.state("liquid")
        stateMachineParser.event("vaporize")

        stateMachineParser.transition("gas")
        stateMachineParser.event("vaporize")

        stateMachineParser.doNotTransition()
        stateMachineParser.event("boil")

        orderVerifier.verify(stateMachineBuilder).addState("liquid")
        orderVerifier.verify(stateMachineBuilder).addEvent("vaporize")
        orderVerifier.verify(stateMachineBuilder).addTransitionToState("gas")
        orderVerifier.verify(stateMachineBuilder).addEvent("vaporize")
        orderVerifier.verify(stateMachineBuilder).addNoTransition()
        orderVerifier.verify(stateMachineBuilder).addEvent("boil")
    }

    @Test
    fun `a Transition can occur only after an event`() {
        val orderVerifier = Mockito.inOrder(stateMachineBuilder)
        stateMachineParser.state("liquid")
        stateMachineParser.event("vaporize")
        stateMachineParser.transition("gas")
        orderVerifier.verify(stateMachineBuilder).addState("liquid")
        orderVerifier.verify(stateMachineBuilder).addEvent("vaporize")
        orderVerifier.verify(stateMachineBuilder).addTransitionToState("gas")
    }


    @Test
    fun `a Transition after a State throws exception `() {
        val exception = assertThrows<StateMachineParserException> {
            stateMachineParser.state("liquid")
            stateMachineParser.transition("gas")
        }
        assertThat(
            exception.message,
            equalTo("A transition can appear only after an event hint:State(name=liquid) appears after Transition(nextState=gas)")
        )
    }

    @Test
    fun `a Transition after previous transition throws exception `() {
        val exception = assertThrows<StateMachineParserException> {
            stateMachineParser.state("liquid")
            stateMachineParser.event("vaporize")
            stateMachineParser.transition("gas")
            stateMachineParser.transition("liquid")
        }
        assertThat(
            exception.message,
            equalTo("A transition can appear only after an event hint:Transition(nextState=gas) appears after Transition(nextState=liquid)")
        )
    }


    @Test
    fun `A Transition after previous doNotTransition throws exception `() {
        assertThrows<StateMachineParserException> {
            stateMachineParser.state("liquid")
            stateMachineParser.event("vaporize")
            stateMachineParser.doNotTransition()
            stateMachineParser.transition("gas")
        }
    }

    @Test
    fun `a DoNotTransition can occur only after an event`() {
        val orderVerifier = Mockito.inOrder(stateMachineBuilder)
        stateMachineParser.state("liquid")
        stateMachineParser.event("warm")
        stateMachineParser.doNotTransition()
        orderVerifier.verify(stateMachineBuilder).addState("liquid")
        orderVerifier.verify(stateMachineBuilder).addEvent("warm")
        orderVerifier.verify(stateMachineBuilder).addNoTransition()
    }

    @Test
    fun `a DoNotTransition after a State throws exception `() {
        assertThrows<StateMachineParserException> {
            stateMachineParser.state("liquid")
            stateMachineParser.doNotTransition()
        }
    }

    @Test
    fun `a DoNotTransition after previous DoNotTransition throws exception `() {
        assertThrows<StateMachineParserException> {
            stateMachineParser.state("liquid")
            stateMachineParser.event("vaporize")
            stateMachineParser.doNotTransition()
            stateMachineParser.doNotTransition()
        }
    }


    @Test
    fun stateMachine() {
        val stateMachine = stateMachineParser.stateMachine()
        verify(stateMachineBuilder).build()
    }
}
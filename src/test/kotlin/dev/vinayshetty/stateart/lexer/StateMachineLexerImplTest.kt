package dev.vinayshetty.stateart.lexer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

internal class StateMachineLexerImplTest {

    @Mock
    private lateinit var stateMachineTokenizer: StateMachineTokenizer
    private lateinit var stateMachineLexer: StateMachineLexer

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        stateMachineLexer = StateMachineLexerImpl(stateMachineTokenizer)
    }

    @Test
    fun lexSimplestStateMachine() {
        val simplestStateMachine = """StateMachine.create<State, Event, SideEffect> {
        initialState(initState)
        state<CurrentState> {
            on<Update> {
                transitionTo(NextState, DoSomeThingCool)
            }
        }"""
        stateMachineLexer.lex(simplestStateMachine)
        verify(stateMachineTokenizer).state("CurrentState")
        verify(stateMachineTokenizer).event("Update")
        verify(stateMachineTokenizer).transition("NextState")
    }

    @Test
    fun lexSingleLineStateMachine() {
        val simplestStateMachine =
            """StateMachine.create<State, Event, SideEffect> {
                |initialState(initState)state<CurrentState>
                |{
                |on<Update> {transitionTo(NextState, DoSomeThingCool)
                |}
                |}""".trimMargin()
        stateMachineLexer.lex(simplestStateMachine)
        verify(stateMachineTokenizer).state("CurrentState")
        verify(stateMachineTokenizer).event("Update")
        verify(stateMachineTokenizer).transition("NextState")
    }

    @Test
    fun lexRandomlyFormattedStateMachine() {
        val simplestStateMachine = """StateMachine.create<State, Event, SideEffect> {
            |initialState(initState)state<CurrentState> {on<Update>
            |{
            |transitionTo(NextState, DoSomeThingCool)
            |}
            |}"""
        stateMachineLexer.lex(simplestStateMachine)
        verify(stateMachineTokenizer).state("CurrentState")
        verify(stateMachineTokenizer).event("Update")
        verify(stateMachineTokenizer).transition("NextState")

    }

    @Test
    fun lexSingleLineStateMachineWithPackageFormatted() {
        val simplestStateMachine =
            """StateMachine.create<dev.vinayshetty.State, dev.vinayshetty.Event, dev.vinayshetty.SideEffect>
                |{
                |initialState(initState)
                |state<dev.vinayshetty.CurrentState> {
                |on<Update> {transitionTo(dev.vinayshetty.NextState(someconstructorparams), DoSomeThingCool)
                |}
                |}""".trimMargin()
        stateMachineLexer.lex(simplestStateMachine)
        verify(stateMachineTokenizer).state("CurrentState")
        verify(stateMachineTokenizer).event("Update")
        verify(stateMachineTokenizer).transition("NextState")
    }

    @Test
    fun lexMultiStateStateMachine() {
        val multiStateMachine = """val stateMachine = StateMachine.create<State, Event, SideEffect> {
        initialState(State.Solid)
        state<State.Solid> {
            on<Event.OnMelted> {
                transitionTo(State.Liquid, SideEffect.LogMelted)
            }
         }
        state<State.Liquid> {
            on<Event.OnFroze> {
                transitionTo(State.Solid, SideEffect.LogFrozen)
            }
            on<Event.OnVaporized> {
                transitionTo(State.Gas, SideEffect.LogVaporized)
            }
        }
        state<State.Gas> {
            on<Event.OnCondensed> {
                transitionTo(State.Liquid, SideEffect.LogCondensed)
            }
        }
        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
            when (validTransition.sideEffect) {
                SideEffect.LogMelted -> logger.log(ON_MELTED_MESSAGE)
                SideEffect.LogFrozen -> logger.log(ON_FROZEN_MESSAGE)
                SideEffect.LogVaporized -> logger.log(ON_VAPORIZED_MESSAGE)
                SideEffect.LogCondensed -> logger.log(ON_CONDENSED_MESSAGE)
            }
        }
        }"""
        stateMachineLexer.lex(multiStateMachine)
        val stateMachineorderVerifier = Mockito.inOrder(stateMachineTokenizer)
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("Solid")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("OnMelted")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("Liquid")
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("Liquid")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("OnFroze")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("Solid")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("OnVaporized")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("Gas")
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("Gas")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("OnCondensed")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("Liquid")
    }
}
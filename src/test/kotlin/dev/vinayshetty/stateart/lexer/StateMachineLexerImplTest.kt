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
                initialState(initState)state<CurrentState>
                {
                on<Update> {transitionTo(NextState, DoSomeThingCool)
                }
                }""".trimMargin()
        stateMachineLexer.lex(simplestStateMachine)
        verify(stateMachineTokenizer).state("CurrentState")
        verify(stateMachineTokenizer).event("Update")
        verify(stateMachineTokenizer).transition("NextState")
    }

    @Test
    fun lexRandomlyFormattedStateMachine() {
        val simplestStateMachine = """StateMachine.create<State, Event, SideEffect> {
            |initialState(initState)state<CurrentState> { on<Update>
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
                {
                initialState(initState)
                state<dev.vinayshetty.CurrentState> {
                on<Update> {transitionTo(dev.vinayshetty.NextState(someconstructorparams), DoSomeThingCool)
                }
                }""".trimMargin()
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


    @Test
    fun lexObjectStateMachine() {
        val multiStateMachine = """ private val stateMachine = StateMachine.create<State, Event, SideEffect> {
            initialState(State.A)
            state<State.A> {
                onExit(onStateAExitListener1)
                onExit(onStateAExitListener2)
                on<Event.E1> {
                    transitionTo(State.B)
                }
                on<Event.E2> {
                    transitionTo(State.C)
                }
                on<Event.E4> {
                    transitionTo(State.D)
                }
            }
            state<State.B> {
                on<Event.E3> {
                    transitionTo(State.C, SideEffect.SE1)
                }
            }
            state<State.C> {
                on<Event.E4> {
                    dontTransition()
                }
                onEnter(onStateCEnterListener1)
                onEnter(onStateCEnterListener2)
            }
            onTransition(onTransitionListener1)
            onTransition(onTransitionListener2)
        }"""
        stateMachineLexer.lex(multiStateMachine)
        val stateMachineorderVerifier = Mockito.inOrder(stateMachineTokenizer)
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("A")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("E1")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("B")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("E2")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("C")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("E4")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("D")
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("B")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("E3")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("C")
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("C")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("E4")
        stateMachineorderVerifier.verify(stateMachineTokenizer).doNotTransition()
    }


    @Test
    fun turnStileStateMachine() {
        val multiStateMachine = """  private val stateMachine2 = StateMachine.create<State, Event, Command> {
            initialState(State.Locked(credit = 0))
            state<State.Locked> {
                on<Event.InsertCoin> {
                    val newCredit = credit + it.value
                    if (newCredit >= FARE_PRICE) {
                        transitionTo(State.Unlocked, Command.OpenDoors)
                    } else {
                        transitionTo(State.Locked(newCredit))
                    }
                }
                on<Event.AdmitPerson> {
                    dontTransition(Command.SoundAlarm)
                }
                on<Event.MachineDidFail> {
                    transitionTo(State.Broken(this), Command.OrderRepair)
                }
            }
            state<State.Unlocked> {
                on<Event.AdmitPerson> {
                    transitionTo(State.Locked(credit = 0), Command.CloseDoors)
                }
            }
            state<State.Broken> {
                on<Event.MachineRepairDidComplete> {
                    transitionTo(oldState)
                }
            }
        } """
        stateMachineLexer.lex(multiStateMachine)
        val stateMachineorderVerifier = Mockito.inOrder(stateMachineTokenizer)
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("Locked")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("InsertCoin")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("Unlocked")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("Locked")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("AdmitPerson")
        stateMachineorderVerifier.verify(stateMachineTokenizer).doNotTransition()
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("MachineDidFail")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("Broken")
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("Unlocked")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("AdmitPerson")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("Locked")
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("Broken")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("MachineRepairDidComplete")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("oldState")
    }

    @Test
    fun constantStateMachine() {
        val constantStateMachine = """ private val stateMachine4 = StateMachine.create<String, Int, String> {
                initialState(STATE_A)
                state(STATE_A) {
                    onExit(onStateAExitListener1)
                    onExit(onStateAExitListener2)
                    on(EVENT_1) {
                        transitionTo(STATE_B)
                    }
                    on(EVENT_2) {
                        transitionTo(STATE_C)
                    }
                    on(EVENT_4) {
                        transitionTo(STATE_D)
                    }
                }
                state(STATE_B) {
                    on(EVENT_3) {
                        transitionTo(STATE_C, SIDE_EFFECT_1)
                    }
                }
                state(STATE_C) {
                    onEnter(onStateCEnterListener1)
                    onEnter(onStateCEnterListener2)
                }
                onTransition(onTransitionListener1)
                onTransition(onTransitionListener2)
            }
           """
        stateMachineLexer.lex(constantStateMachine)
        val stateMachineorderVerifier = Mockito.inOrder(stateMachineTokenizer)
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("STATE_A")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("EVENT_1")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("STATE_B")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("EVENT_2")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("STATE_C")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("EVENT_4")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("STATE_D")
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("STATE_B")
        stateMachineorderVerifier.verify(stateMachineTokenizer).event("EVENT_3")
        stateMachineorderVerifier.verify(stateMachineTokenizer).transition("STATE_C")
        stateMachineorderVerifier.verify(stateMachineTokenizer).state("STATE_C")






    }
}
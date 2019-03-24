package dev.vinayshetty.stateart.dotsbuilder

import dev.vinayshetty.stateart.datastructure.Event
import dev.vinayshetty.stateart.datastructure.State
import dev.vinayshetty.stateart.datastructure.StateMachine
import dev.vinayshetty.stateart.datastructure.Transition
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DotsBuilderImplTest {

    private lateinit var dotsBuilder: DotsBuilder
    @BeforeEach
    internal fun setUp() {
        dotsBuilder = DotsBuilderImpl()
    }

    @Test
    fun `dots builder with empty state machine`() {
        val emptyDotString = dotsBuilder.build("empty", StateMachine(setOf()))
        val expected = """
        digraph empty {
        }
        """.trimIndent()
        assertThat(emptyDotString, equalTo(expected))
    }

    @Test
    fun `dots builder with multiple empty state machine should be empty dot string`() {
        val states = setOf(State("one", setOf()), State("two", setOf()), State("three", setOf()))
        val emptyDotString = dotsBuilder.build("empty", StateMachine(states))
        val expected = """
        digraph empty {
        }
        """.trimIndent()
        assertThat(emptyDotString, equalTo(expected))
    }

    @Test
    fun `dots builder with single state and transitions`() {
        val transition = Transition(Event("event"), "two")
        val anotherTransition = Transition(Event("other event"), "three")
        val state = State("one", setOf(transition, anotherTransition))
        val states = setOf(state)
        val emptyDotString = dotsBuilder.build("empty", StateMachine(states))
        val expected = """
        digraph empty {
         one -> two [label="event"]
         one -> three [label="other event"]
        }
        """.trimIndent()
        assertThat(emptyDotString, equalTo(expected))
    }


    @Test
    fun `dots builder with multiple states and multiple transitions`() {
        val transitionTwo = Transition(Event("event"), "two")
        val transitionThree = Transition(Event("other event"), "three")
        val transitionFour = Transition(Event("event four"), "four")
        val transitionFive = Transition(Event("event five"), "five")
        val state = State("one", setOf(transitionTwo, transitionThree))
        val anotherState = State("another", setOf(transitionFour, transitionFive))
        val states = setOf(state, anotherState)
        val emptyDotString = dotsBuilder.build("empty", StateMachine(states))
        val expected = """
        digraph empty {
         one -> two [label="event"]
         one -> three [label="other event"]
         another -> four [label="event four"]
         another -> five [label="event five"]
        }
        """.trimIndent()
        assertThat(emptyDotString, equalTo(expected))
    }
}
package dev.vinayshetty.stateart.lexer

import dev.vinayshetty.stateart.datastructure.StateMachine

class StateMachineLexerImpl(private val stateMachineTokenizer: StateMachineTokenizer) : StateMachineLexer {
    private companion object {
        private val stateMatcherRegex = ".*state(\\<|\\()(.*?)(\\>|\\)).*".toRegex()
        private val eventMatcherRegex = ".*\\s+on(\\<|\\()(.*?)(\\>|\\)).*".toRegex()
        private val transitionToMatcherRegex = ".*transitionTo\\((.*?)(,|\\)).*".toRegex()
        private val doNotTransitionMatcherRegex = ".*dontTransition\\((.*?)\\).*".toRegex()
    }

    override fun lex(stateMachineString: String): StateMachine {
        stateMachineString.split("\n").forEach { lexLine(it) }
        return stateMachineTokenizer.stateMachine()
    }

    private fun lexLine(line: String) {
        if (line.matches(stateMatcherRegex)) {
            val matchResult = stateMatcherRegex.find(line)
            matchResult?.let {
                val (_, state, _) = it.destructured
                val shortName = shortenName(state)
                stateMachineTokenizer.state(shortName)
            }

        }
        if (line.matches(doNotTransitionMatcherRegex)) {
            val matchResult = doNotTransitionMatcherRegex.find(line)
            matchResult?.let {
                stateMachineTokenizer.doNotTransition()
            }
        }
        if (line.matches(eventMatcherRegex)) {
            val matchResult = eventMatcherRegex.find(line)
            matchResult?.let {
                val (_, event, _) = it.destructured
                val shortName = shortenName(event)
                stateMachineTokenizer.event(shortName)
            }
        }
        if (line.matches(transitionToMatcherRegex)) {
            val matchResult = transitionToMatcherRegex.find(line)
            matchResult?.let {
                val (toState) = it.destructured
                val shortName = shortenName(toState)
                stateMachineTokenizer.transition(shortName)
            }
        }

    }

    private fun shortenName(name: String): String {
        return when {
            name.contains("(") -> {
                val nameWithoutBraces = name.split("(").first()
                return nameWithoutBraces.split(".").last()
            }
            name.contains(".") -> return name.split(".").last()
            else -> name
        }


    }
}
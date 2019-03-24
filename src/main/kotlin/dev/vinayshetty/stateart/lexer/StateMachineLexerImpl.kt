package dev.vinayshetty.stateart.lexer

import dev.vinayshetty.stateart.datastructure.StateMachine

class StateMachineLexerImpl(private val stateMachineTokenizer: StateMachineTokenizer) : StateMachineLexer {
    private companion object {
        private val stateMatcherRegex = ".*state\\<(.*?)\\>.*".toRegex()
        private val eventMatcherRegex = ".*on\\<(.*?)\\>.*".toRegex()
        private val transitionToMatcherRegex = ".*transitionTo\\((.*?),.*".toRegex()
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
                val (state) = it.destructured
                val shortName = shortenName(state)
                stateMachineTokenizer.state(shortName)
            }

        }
        if (line.matches(eventMatcherRegex)) {
            val matchResult = eventMatcherRegex.find(line)
            matchResult?.let {
                val (event) = it.destructured
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
        if (line.matches(doNotTransitionMatcherRegex)) {
            val matchResult = doNotTransitionMatcherRegex.find(line)
            matchResult?.let {
                stateMachineTokenizer.doNotTransition()
            }
        }
    }

    private fun shortenName(name: String): String {
        return if (!name.contains("."))
            return name
        else {
            val simpleName = name.split(".").last()
            if (simpleName.contains("(")) {
                return simpleName.split("(").first()
            } else simpleName
        }

    }
}
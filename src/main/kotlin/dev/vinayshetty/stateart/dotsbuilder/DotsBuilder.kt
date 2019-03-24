package dev.vinayshetty.stateart.dotsbuilder

import dev.vinayshetty.stateart.datastructure.StateMachine

interface DotsBuilder {
    fun build(name: String, stateMachine: StateMachine): String
}
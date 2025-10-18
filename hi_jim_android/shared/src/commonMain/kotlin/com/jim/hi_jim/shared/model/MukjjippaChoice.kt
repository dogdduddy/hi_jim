package com.jim.hi_jim.shared.model

enum class MukjjippaChoice(val displayName: String) {
    ROCK("묵"),
    SCISSORS("찌"),
    PAPER("빠");

    fun getCountdownMessage(): String {
        return when (this) {
            ROCK -> "묵에"
            SCISSORS -> "찌에"
            PAPER -> "빠에"
        }
    }

    fun beats(other: MukjjippaChoice): Boolean {
        return when (this) {
            ROCK -> other == SCISSORS
            SCISSORS -> other == PAPER
            PAPER -> other == ROCK
        }
    }
}
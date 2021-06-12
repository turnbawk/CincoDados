package com.turnbawk.cincodados


class CincoDadosGame {
    private var dice = listOf(Die(), Die(), Die(), Die(), Die())
    private var scores = mutableListOf<Int>()

    fun upperSectionDelta(selectedScore: Int): Int {
        var delta = 0
        for(i in ONES..SIXES) {
            if(scores[i] != NOT_LOCKED) delta += scores[i] - (3 * (i + 1))
        }

        // Preview changes in delta if locking in upper section
        if(selectedScore in ONES..SIXES) {
            delta += sumOfDiceRolledAs(selectedScore + 1) - (3 * (selectedScore + 1))
        }
        return delta
    }

    fun upperSectionTotal(): Int {
        var sum = 0
        for(i in ONES..SIXES) {
            if(scores[i] != NOT_LOCKED) sum += scores[i]
        }
        return sum
    }

    fun lowerSectionTotal(): Int {
        var sum = 0
        for(i in THREEOFAKIND..CHANCE) {
            if(scores[i] != NOT_LOCKED) sum += scores[i]
        }
        return sum
    }

    fun cincoDadosBonus(): Int {
        return scores[CINCOBONUS]
    }

    fun upperSectionBonus(): Int {
        return if(upperSectionTotal() >= 63) 35 else 0
    }

    fun rollValue(scoreIndex: Int, lockValueAsScore: Boolean): Int {
        var score = 0
        when(scoreIndex) {
            in ONES..SIXES ->
                score = sumOfDiceRolledAs(scoreIndex + 1)
            THREEOFAKIND ->
                score = if(maxDuplicates() >= 3) sumOfDice() else 0
            FOUROFAKIND ->
                score = if(maxDuplicates() >= 4) sumOfDice() else 0
            FULLHOUSE ->
                score = if(isDingDong() || potentialMultipleCincoDados()) 25 else 0
            SMALLSTRAIGHT ->
                score = if(maxSequentialDice() >= 4 || potentialMultipleCincoDados()) 30 else 0
            LARGESTRAIGHT ->
                score = if(maxSequentialDice() == 5 || potentialMultipleCincoDados()) 40 else 0
            CINCODADOS ->
                score = if(maxDuplicates() == 5) 50 else 0
            CHANCE ->
                score = sumOfDice()
        }

        if(lockValueAsScore) {
            if(potentialMultipleCincoDados()) scores[CINCOBONUS] += 100
            scores[scoreIndex] = score
        }
        return score
    }

    fun lockedScore(scoreIndex: Int): Int {
        return scores[scoreIndex]
    }

    private fun sumOfDice(): Int {
        var sum = 0
        dice.forEach {
            sum += it.getValue()
        }
        return sum
    }

    private fun sumOfDiceRolledAs(i: Int): Int {
        var sum = 0
        dice.forEach {
            if(it.getValue() == i) sum += i
        }
        return sum
    }

    fun isHeld(i: Int): Boolean {
        return dice[i].isHeld()
    }

    fun toggleHold(i: Int) {
        dice[i].toggleHold()
    }

    fun dieValue(i: Int): Int {
        return dice[i].getValue()
    }

    fun roll() {
        dice.forEach { it.roll() }
    }

    private fun maxDuplicates(): Int {
        var dups = 1
        var maxDups = 1
        val sortedDice = listOf(dice[0].getValue(), dice[1].getValue(),dice[2].getValue(), dice[3].getValue(),dice[4].getValue()).sorted()

        for(i in 1..4) {
            if(sortedDice[i - 1] == sortedDice[i]) {
                dups++
            } else {
                if(dups > maxDups) maxDups = dups
                dups = 1
            }
        }
        if(dups > maxDups) maxDups = dups
        return maxDups
    }

    private fun potentialMultipleCincoDados(): Boolean {
        return (scores[CINCODADOS] > 0 && maxDuplicates() == 5)
    }

    private fun sortedDice(): List<Int> {
        return listOf(dice[0].getValue(), dice[1].getValue(),dice[2].getValue(), dice[3].getValue(),dice[4].getValue()).sorted()
    }

    private fun isDingDong(): Boolean {
        if(maxDuplicates() == 3) {
            val sortedDice = sortedDice()
            return ((sortedDice[0] == sortedDice[1]) && (sortedDice[3] == sortedDice[4]))
        }
        return false
    }

    private fun maxSequentialDice(): Int {
        var seq = 1
        var maxSeq = 1
        val sortedDice = sortedDice()

        for(i in 1..4) {
            if(sortedDice[i] == sortedDice[i - 1] + 1) {
                seq++
            } else if(sortedDice[i] != sortedDice[i - 1]){
                if(seq > maxSeq) maxSeq = seq
                seq = 1
            }
        }
        if(seq > maxSeq) maxSeq = seq
        return maxSeq
    }

    fun contains(i: Int): Boolean {
        dice.forEach {
            if(it.getValue() == i) return true
        }
        return false
    }

    fun releaseAll() {
        dice.forEach { it.release() }
    }

    // Returns true if all score categories have been locked
    fun gameOver(): Boolean {
        return !scores.contains(NOT_LOCKED)
    }

    fun restart() {
        // Set dice to 1-5 so it looks good when we start
        for(die in 0..4) {
            dice[die].setValue(die + 1)
        }

        releaseAll()

        // Reset all scores to NOT_LOCKED status and set
        // Cinco Dados Bonus value to zero.
        scores = mutableListOf(
            NOT_LOCKED, NOT_LOCKED, NOT_LOCKED,
            NOT_LOCKED, NOT_LOCKED, NOT_LOCKED,
            NOT_LOCKED, NOT_LOCKED, NOT_LOCKED,
            NOT_LOCKED, NOT_LOCKED, NOT_LOCKED,
            NOT_LOCKED, 0
        )
    }

    init {
        restart()
    }

}

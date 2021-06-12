package com.turnbawk.cincodados

class Die {
    private var value: Int = 0
    private var held: Boolean = false

    fun getValue(): Int {
        return value
    }

    fun setValue(v: Int) {
        value = v
    }

    fun toggleHold() {
        held = !held
    }

    fun isHeld(): Boolean {
        return held
    }

    fun roll() {
        if(!held) value = (1..6).random()
    }

    fun release() {
        held = false
    }
}
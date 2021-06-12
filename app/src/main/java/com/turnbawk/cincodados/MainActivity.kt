package com.turnbawk.cincodados

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var dice = CincoDadosGame()
    private var dicePlayable = false

    private var scoreColor = Color.WHITE
    private var scoreHighlightColor = Color.YELLOW
    private var scoreLockedColor = Color.GREEN
    private var scoreSelectedColor = Color.CYAN
    private var deltaPosColor = Color.GREEN
    private var deltaNegColor = Color.RED

    private var selectedScore = NO_TARGET
    private var rollCount = 1

    private var dieImages = listOf(
        R.drawable.roll1,
        R.drawable.roll2,
        R.drawable.roll3,
        R.drawable.roll4,
        R.drawable.roll5,
        R.drawable.roll6
    )

    private val scoreStrings = listOf(
        R.string.ones,
        R.string.twos,
        R.string.threes,
        R.string.fours,
        R.string.fives,
        R.string.sixes,
        R.string._3_of_a_kind,
        R.string._4_of_a_kind,
        R.string.ding_dong,
        R.string.sm_straight,
        R.string.lg_straight,
        R.string.cinco_dados,
        R.string.chance,
        R.string.cinco_bonus,
    )

    private lateinit var rollButton: Button
    private lateinit var dieDisplays: List<ImageView>
    private lateinit var scoreDisplays: List<TextView>
    private lateinit var statusDisplays: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        find_UI_Elements()
        setDieOnClickListeners()
        setScoreDisplayOnClickListeners()
        updateDisplay()
    }

    private fun setScoreDisplayOnClickListeners() {
        for(index in ONES..CHANCE) {
            scoreDisplays[index].setOnClickListener {
                if(dicePlayable) {
                    if (selectedScore != index && dice.lockedScore(index) == NOT_LOCKED) {
                        selectedScore = index
                    } else {
                        selectedScore = NO_TARGET
                    }
                    updateDisplay()
                }
            }
        }
    }

    private fun setDieOnClickListeners() {
        for (die in 0..4) {
            dieDisplays[die].setOnClickListener {
                if(dicePlayable) {
                    dice.toggleHold(die)
                    updateDisplay()
                }
            }
        }
    }

    private fun find_UI_Elements() {
        rollButton = findViewById(R.id.bRoll)

        dieDisplays = listOf(
            findViewById(R.id.d1),
            findViewById(R.id.d2),
            findViewById(R.id.d3),
            findViewById(R.id.d4),
            findViewById(R.id.d5)
        )

        scoreDisplays = listOf(
            findViewById(R.id.tOnes),
            findViewById(R.id.tTwos),
            findViewById(R.id.tThrees),
            findViewById(R.id.tFours),
            findViewById(R.id.tFives),
            findViewById(R.id.tSixes),
            findViewById(R.id.t3k),
            findViewById(R.id.t4k),
            findViewById(R.id.tDD),
            findViewById(R.id.tSmallS),
            findViewById(R.id.tLargeS),
            findViewById(R.id.tCinco),
            findViewById(R.id.tChance)
        )

        statusDisplays = listOf(
            findViewById(R.id.tUpperTotal),
            findViewById(R.id.tUpperBonus),
            findViewById(R.id.tUpperDelta),
            findViewById(R.id.tLowerTotal),
            findViewById(R.id.tCincoBonus),
            findViewById(R.id.tGrandTotal)
        )
    }

    fun rollButtonClicked(v: View) {
        if (selectedScore == NO_TARGET) {
            if(dice.gameOver()) {
                dice.restart()
                updateDisplay()
            } else {
                roll()
            }
        } else {
            dice.rollValue(selectedScore, true)
            dice.releaseAll()
            rollCount = 1
            selectedScore = NO_TARGET
            dicePlayable = false
            updateDisplay()
        }
    }

    private fun scoreSetHighlight(scoreIndex: Int, highlight: Boolean) {
        if (dice.lockedScore(scoreIndex) == NOT_LOCKED) {
            if(selectedScore == scoreIndex) {
                scoreDisplays[scoreIndex].setTextColor(scoreSelectedColor)
            } else if(highlight) {
                scoreDisplays[scoreIndex].setTextColor(scoreHighlightColor)
            } else {
                scoreDisplays[scoreIndex].setTextColor(scoreColor)
            }
        } else {
            scoreDisplays[scoreIndex].setTextColor(scoreLockedColor)
        }
    }

    private fun updateDisplay() {
        if(dicePlayable) {
            if (selectedScore == NO_TARGET) {
                if (rollCount <= 3) {
                    rollButton.text = getString(R.string.roll, rollCount)
                } else {
                    rollButton.text = getString(R.string.outOfRolls)
                }
            } else {
                rollButton.text = getString(R.string.confirm)
            }
        } else if(dice.gameOver()) {
            rollButton.text=getString(R.string.gameover)
        } else {
            rollButton.text = getString(R.string.mustroll)
        }

        for(dieDisplay in 0..4) {
            dieDisplays[dieDisplay].setImageResource(dieImages[dice.dieValue(dieDisplay) - 1])
            dieDisplays[dieDisplay].setColorFilter(Color.argb(if (dice.isHeld(dieDisplay)) 200 else 0, 0, 0, 0))
        }

        for(i in ONES..CHANCE) {
            if(dice.lockedScore(i) == NOT_LOCKED) {
                val potentialScore = dice.rollValue(i, false)
                scoreDisplays[i].text = getString(scoreStrings[i], if(dice.lockedScore(i) == NOT_LOCKED) potentialScore else dice.lockedScore(i))
                scoreSetHighlight(i, (potentialScore > 0 && dicePlayable))
            } else {
                scoreDisplays[i].text = getString(scoreStrings[i], dice.lockedScore(i))
                scoreSetHighlight(i, false)
            }
        }

        statusDisplays[0].text = getString(R.string.utotal, dice.upperSectionTotal())
        statusDisplays[1].text = getString(R.string.upperbonus, dice.upperSectionBonus())

        val delta = dice.upperSectionDelta(selectedScore)
        if(delta < 1) {
            statusDisplays[2].text = getString(R.string.upperdeltaNeg, delta)
        } else {
            statusDisplays[2].text = getString(R.string.upperdeltaPos, delta)
        }
        when {
            delta < 0 -> statusDisplays[2].setTextColor(deltaNegColor)
            delta == 0 -> statusDisplays[2].setTextColor(scoreColor)
            delta > 0 -> statusDisplays[2].setTextColor(deltaPosColor)
        }

        statusDisplays[3].text = getString(R.string.ltotal, dice.lowerSectionTotal())
        statusDisplays[4].text = getString(R.string.cinco_bonus, dice.cincoDadosBonus())
        statusDisplays[5].text = getString(R.string.grand_total, dice.upperSectionTotal() + dice.upperSectionBonus() + dice.lowerSectionTotal() + dice.cincoDadosBonus())
    }

    private fun roll() {
        if(rollCount <= 3) {
            selectedScore = NO_TARGET
            dice.roll()
            dicePlayable = true
            rollCount++
            updateDisplay()
        }
    }
}

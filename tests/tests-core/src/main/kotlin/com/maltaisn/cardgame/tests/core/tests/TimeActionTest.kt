package com.maltaisn.cardgame.tests.core.tests

import com.badlogic.gdx.math.Interpolation
import com.maltaisn.cardgame.core.PCard
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.TimeAction
import com.maltaisn.cardgame.widget.card.CardActor
import ktx.actors.alpha
import ktx.log.info
import kotlin.math.max
import kotlin.math.min


/**
 * Test time action and its global speed modifier.
 */
class TimeActionTest : ActionBarTest() {

    private var rotateAction: TimeAction? = null
        set(value) {
            if (field != null) root.removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val card = CardActor(coreSkin, cardSkin, PCard(8, PCard.CLUB))
        card.setOrigin(card.prefWidth / 2, card.prefHeight / 2)
        layout.gameLayer.centerTable.add(card).expand()

        var cardShown = true
        var intpIn = 0
        var intpOut = 0

        addActionBtn("Fade") {
            cardShown = !cardShown
            if (rotateAction == null) {
                rotateAction = object : TimeAction(1f,
                        INTERPOLATIONS[intpIn].second,
                        INTERPOLATIONS[intpOut].second,
                        reversed = !cardShown) {

                    override fun begin() {
                        card.isVisible = true
                        card.alpha = if (cardShown) 0f else 1f
                        info { "Animation began" }
                    }

                    override fun update(progress: Float) {
                        reversed = !cardShown
                        card.alpha = progress
                    }

                    override fun end() {
                        card.isVisible = cardShown
                        rotateAction = null
                        info { "Animation ended" }
                    }
                }
                card.addAction(rotateAction)
            }
        }

        // Interpolation
        addActionBtn("In: ${INTERPOLATIONS[intpIn].first}") {
            intpIn = (intpIn + 1) % INTERPOLATIONS.size
            val intp = INTERPOLATIONS[intpIn]
            it.title = "In: ${intp.first}"
            rotateAction?.end()
        }
        addActionBtn("Out: ${INTERPOLATIONS[intpOut].first}") {
            intpOut = (intpOut + 1) % INTERPOLATIONS.size
            val intp = INTERPOLATIONS[intpOut]
            it.title = "Out: ${intp.first}"
            rotateAction?.end()
        }

        // Speed
        addActionBtn("Speed -") {
            rotateAction?.end()
            TimeAction.SPEED_MULTIPLIER = max(0.125f, TimeAction.SPEED_MULTIPLIER / 2)
        }
        addActionBtn("Speed +") {
            rotateAction?.end()
            TimeAction.SPEED_MULTIPLIER = min(8f, TimeAction.SPEED_MULTIPLIER * 2)
        }
    }

    companion object {
        private val INTERPOLATIONS = listOf(
                "Linear" to Interpolation.linear,
                "Smooth" to Interpolation.smooth,
                "Pow" to Interpolation.pow4,
                "Bounce" to Interpolation.bounce
        )
    }

}
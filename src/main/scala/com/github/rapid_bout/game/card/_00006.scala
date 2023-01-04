package com.github.rapid_bout.game.card

import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.process.Process
import com.github.rapid_bout.game.effect.process.action.Draw
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00006 extends Card {
  override val point: Int = 0
  override val effect: Effect = (_: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      Draw(initiator, number = 2)
    )
}

package com.github.rapid_bout.game.card

import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.action
import com.github.rapid_bout.game.effect.action.Process
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _JOKER extends Card {
  override val point: Int = 0

  override val effect: Effect = (game: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      action.ReverseAllWithFront(initiator)
    )
}

package com.github.rapid_bout.game.card.archive

import com.github.rapid_bout.game.card.Card
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.process.action.ReverseAll
import com.github.rapid_bout.game.effect.process.{Process, action}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _JOKER_1 extends Card {
  override val point: Int = 0

  override val effect: Effect = (game: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      ReverseAll(game.getOpponent(initiator).key),
      action.ReverseAll(initiator)
    )
}

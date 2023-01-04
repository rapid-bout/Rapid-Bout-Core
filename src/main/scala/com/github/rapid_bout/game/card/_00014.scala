package com.github.rapid_bout.game.card

import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.action
import com.github.rapid_bout.game.effect.action.Process.BoolSelect
import com.github.rapid_bout.game.effect.action.{Optional, Process}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00014 extends Card {
  override val point: Int = 7
  override val preprocess: Effect = (game: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      // カードを引くか？
      BoolSelect(game.getOpponent(initiator).key),
      Optional(action.Draw(game.getOpponent(initiator).key, number = 1)),
      // カードを引くか？
      BoolSelect(game.getOpponent(initiator).key),
      action.Optional(action.Draw(game.getOpponent(initiator).key, number = 1))
    )
}

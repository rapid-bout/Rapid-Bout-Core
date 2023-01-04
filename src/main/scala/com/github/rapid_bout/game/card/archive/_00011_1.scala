package com.github.rapid_bout.game.card.archive

import com.github.rapid_bout.game.card.Card
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.process.Process
import com.github.rapid_bout.game.effect.process.action.AddPlayCount
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00011_1 extends Card {
  override val point: Int = 0
  override val effect: Effect = (_: Game, _: PlayerKey, _: MutableCard) =>
    List[Process](
      // プレイ権を2増やす
      AddPlayCount(),
      AddPlayCount()
    )
}

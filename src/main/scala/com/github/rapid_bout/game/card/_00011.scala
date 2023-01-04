package com.github.rapid_bout.game.card

import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.action.{AddPlayCount, Process}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00011 extends Card {
  override val point: Int = 0
  override val effect: Effect = (_: Game, _: PlayerKey, _: MutableCard) =>
    List[Process](
      // プレイ権を1増やす
      AddPlayCount()
    )
}

package com.github.rapid_bout.game.card

import com.github.rapid_bout.game.Zone.Hand
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.process.Process
import com.github.rapid_bout.game.effect.process.action.PutOfHand
import com.github.rapid_bout.game.effect.process.select.{Const, RandomSelect}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00002 extends Card {
  override val point: Int = 3
  override val effect: Effect = (game: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      // 相手の手札をランダムに1枚選ぶ
      RandomSelect(initiator, game.getOpponent(initiator).key, Hand, number = 1, duplicate = false),
      // 裏向き
      Const(true),
      // 手札を場に置く
      PutOfHand(game.getOpponent(initiator).key)
    )
}

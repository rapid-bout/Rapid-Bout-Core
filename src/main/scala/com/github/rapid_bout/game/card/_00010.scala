package com.github.rapid_bout.game.card
import com.github.rapid_bout.game.PlayerKey.Both
import com.github.rapid_bout.game.Zone.Field
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.action
import com.github.rapid_bout.game.effect.action.{Bounce, Process}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

import Process.UserSelect

object _00010 extends Card {
  override val point: Int = 0
  override val effect: Effect = (_: Game, initiator: PlayerKey, _: MutableCard) =>
    List[action.Process](
      // 場のカードを1枚選ぶ
      UserSelect(initiator, Both, Field, number = 1, opt = false, duplicate = false),
      // 場のカードを手札に戻す
      Bounce
    )
}

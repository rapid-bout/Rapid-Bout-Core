package com.github.rapid_bout.game.card
import com.github.rapid_bout.game.PlayerKey.Both
import com.github.rapid_bout.game.Zone.Field
import com.github.rapid_bout.game.effect.Effect.{DoNothing, Effect}
import com.github.rapid_bout.game.effect.Term.MoreHand
import com.github.rapid_bout.game.effect.action.{Process, ReturnOfField}
import com.github.rapid_bout.game.effect.{Term, action}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

import Process.UserSelect

object _00016 extends Card {
  override val point: Int = 0
  override val term: Term = MoreHand(2)
  override val preprocess: Effect = (_: Game, initiator: PlayerKey, _: MutableCard) =>
    List[action.Process](
      // 場のカードを1枚選ぶ
      UserSelect(initiator, Both, Field, number = 1, opt = false, duplicate = false),
      // 場のカードを山札に返す
      ReturnOfField
    )
  override val effect: Effect = DoNothing
}

package com.github.rapid_bout.game.card

import com.github.rapid_bout.game.Zone.Hand
import com.github.rapid_bout.game.effect.Effect.{DoNothing, Effect}
import com.github.rapid_bout.game.effect.Term.MoreHand
import com.github.rapid_bout.game.effect.action.Process
import com.github.rapid_bout.game.effect.action.Process.UserSelect
import com.github.rapid_bout.game.effect.{Term, action}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00003 extends Card {
  override val point: Int = 6
  override val term: Term = MoreHand(2)
  override val preprocess: Effect = (_: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      // 手札のカードを1枚選ぶ
      UserSelect(initiator, initiator, Hand, number = 1, opt = false, duplicate = false),
      // 手札を山札に返す
      action.ReturnOfHand(initiator)
    )
  override val effect: Effect = DoNothing
}

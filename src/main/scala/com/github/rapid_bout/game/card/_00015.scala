package com.github.rapid_bout.game.card

import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.Term.MoreHand
import com.github.rapid_bout.game.effect.action.Process
import com.github.rapid_bout.game.effect.{Term, action}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

import Process.Const

object _00015 extends Card {
  override val point: Int = 2
  override val term: Term = MoreHand(1)
  override val effect: Effect = (_: Game, initiator: PlayerKey, _: MutableCard) =>
    List[action.Process](
      // 山札一番上を裏向きで置く
      Const(Seq(1)),
      Const(true),
      action.PutOfDeck(initiator),
      // カードを1枚引く
      action.Draw(initiator, number = 1)
    )
}

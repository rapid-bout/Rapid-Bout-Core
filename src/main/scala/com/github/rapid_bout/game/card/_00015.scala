package com.github.rapid_bout.game.card

import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.Term
import com.github.rapid_bout.game.effect.Term.MoreHand
import com.github.rapid_bout.game.effect.process.Process
import com.github.rapid_bout.game.effect.process.action.{Draw, PutOfDeck}
import com.github.rapid_bout.game.effect.process.select.Const
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00015 extends Card {
  override val point: Int = 2
  override val term: Term = MoreHand(1)
  override val effect: Effect = (_: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      // 山札一番上を裏向きで置く
      Const(Seq(1)),
      Const(true),
      PutOfDeck(initiator),
      // カードを1枚引く
      Draw(initiator, number = 1)
    )
}

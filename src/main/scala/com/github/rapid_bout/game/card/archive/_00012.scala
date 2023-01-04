package com.github.rapid_bout.game.card.archive

import com.github.rapid_bout.game.card.Card
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.Term.Forbidden
import com.github.rapid_bout.game.effect.action.Process.Const
import com.github.rapid_bout.game.effect.action.{Process, ReturnOfField, ReturnOfStack}
import com.github.rapid_bout.game.effect.{CounterTerm, Term}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00012 extends Card {
  override val point: Int = 0
  override val term: Term = Forbidden
  override val counterTerm: CounterTerm = (_: Game, _: PlayerKey, willPlay: MutableCard) => {
    willPlay.point == 0
  }
  override val effect: Effect = {
    (game: Game, initiator: PlayerKey, parent: MutableCard) =>
      List[Process](
        // スタックに乗っているカードを山札に戻す
        ReturnOfStack(),
        // 場のこのカードを1枚選ぶ
        Const(game.selectField(initiator, parent)),
        Const(initiator),
        // 場のカードを山札に返す
        ReturnOfField
      )
  }
}

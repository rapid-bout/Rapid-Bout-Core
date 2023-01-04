package com.github.rapid_bout.game.card
import com.github.rapid_bout.game.Zone.Hand
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.Term.MoreHand
import com.github.rapid_bout.game.effect.action.Process.UserSelect
import com.github.rapid_bout.game.effect.{Term, action}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00007 extends Card {
  override val point: Int = 0
  override val term: Term = MoreHand(2)
  override val preprocess: Effect = (game: Game, initiator: PlayerKey, _: MutableCard) =>
    List[action.Process](
      // 手札のカードを1枚選ぶ
      UserSelect(initiator, initiator, Hand, number = 1, opt = false, duplicate = false),
      // 手札を山札に返す
      action.ReturnOfHand(initiator)
    )
  override val effect: Effect = (game: Game, initiator: PlayerKey, _: MutableCard) =>
    List[action.Process](
      action.Draw(initiator, number = 3)
    )
}

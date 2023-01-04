package com.github.rapid_bout.game.card
import com.github.rapid_bout.game.Zone.Hand
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.process.action.PutOfHand
import com.github.rapid_bout.game.effect.process.select.{Const, UserSelect}
import com.github.rapid_bout.game.effect.process.{Process, action}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey, Zone}

object _00008 extends Card {
  override val point: Int = 2
  override val effect: Effect = (game: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      // 手札のカードを1枚選ぶ
      UserSelect(initiator, initiator, Hand, number = 1, opt = false, duplicate = false),
      // 裏向き
      Const(true),
      // 手札を場に置く
      PutOfHand(initiator),
      // 相手が手札のカードを1枚選ぶ
      UserSelect(
        game.getOpponent(initiator).key,
        game.getOpponent(initiator).key,
        Zone.Hand,
        number = 1,
        opt = false,
        duplicate = false
      ),
      // 裏向き
      Const(true),
      // 相手は手札を場に置く
      action.PutOfHand(game.getOpponent(initiator).key)
    )
}

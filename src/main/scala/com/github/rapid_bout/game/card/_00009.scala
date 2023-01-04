package com.github.rapid_bout.game.card
import com.github.rapid_bout.game.Zone.Hand
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.process.Process
import com.github.rapid_bout.game.effect.process.action.ReturnOfHand
import com.github.rapid_bout.game.effect.process.select.RandomSelect
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00009 extends Card {
  override val point: Int = 0
  override val effect: Effect = (game: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      // 自分が相手の手札のカードをランダムに1枚選ぶ
      RandomSelect(
        initiator,
        game.getOpponent(initiator).key,
        Hand,
        number = 1,
        duplicate = false
      ),
      // 相手は手札を山札に返す
      ReturnOfHand(game.getOpponent(initiator).key)
    )
}

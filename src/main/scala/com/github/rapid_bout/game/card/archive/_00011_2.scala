package com.github.rapid_bout.game.card.archive

import com.github.rapid_bout.game.Zone.Hand
import com.github.rapid_bout.game.card.Card
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.action
import com.github.rapid_bout.game.effect.action.Process.UserSelect
import com.github.rapid_bout.game.effect.action.{AddPlayCount, Process}
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object _00011_2 extends Card {
  override val point: Int = 0
  override val effect: Effect = (_: Game, initiator: PlayerKey, _: MutableCard) =>
    List[Process](
      // 2枚引く
      action.Draw(initiator, number = 2),
      // 自分の手札を2枚選ぶ
      UserSelect(initiator, initiator, Hand, number = 2, opt = false, duplicate = false),
      // 手札を山札に戻す
      action.ReturnOfHand(initiator),
      // プレイ権を1増やす
      AddPlayCount()
    )
}

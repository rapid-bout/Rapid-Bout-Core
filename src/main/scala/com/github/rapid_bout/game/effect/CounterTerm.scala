package com.github.rapid_bout.game.effect

import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

/** 対抗で使用するための条件 */
trait CounterTerm {

  /**
    * 対抗条件を満たしているか判定する
    * @param game 現在のゲーム盤面
    * @param initiator 対抗する側のプレイヤー
    * @param willPlay スタックに積まれているカード
    * @return 対抗条件を満たしているか
    */
  def verify(game: Game, initiator: PlayerKey, willPlay: MutableCard): Boolean
}

object CounterTerm {

  /** 対抗不可 */
  case object Forbidden extends CounterTerm {

    /** @inheritdoc */
    def verify(game: Game, initiator: PlayerKey, willPlay: MutableCard): Boolean = false
  }
}

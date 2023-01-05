package com.github.rapid_bout.game.effect

import com.github.rapid_bout.game.{Game, PlayerKey}

/** カードを使用する条件 */
trait Term {

  /**
    * カードを使用する条件を満たしているか判定する
    * @param game 現在のゲーム盤面
    * @param initiator カードを使用する側のプレイヤー
    * @return カードを使用する条件を満たしているか
    */
  def verify(game: Game, initiator: PlayerKey): Boolean
}

object Term {

  /** 条件なし (常に満たしている) */
  case object AnyTime extends Term {

    /** @inheritdoc */
    override def verify(game: Game, initiator: PlayerKey): Boolean = true
  }

  /**
    * 手札が指定枚数以上ある
    * @param count 指定枚数 (プレイ前のこのカードを含む)
    */
  case class MoreHand(count: Int) extends Term {

    /** @inheritdoc */
    override def verify(game: Game, initiator: PlayerKey): Boolean =
      game.get(initiator).hand.count >= count
  }

  /** 常に発動できない */
  case object Forbidden extends Term {

    /** @inheritdoc */
    def verify(game: Game, initiator: PlayerKey): Boolean = false
  }
}

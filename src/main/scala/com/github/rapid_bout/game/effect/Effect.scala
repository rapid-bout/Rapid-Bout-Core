package com.github.rapid_bout.game.effect

import com.github.rapid_bout.game.effect.process.Process
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object Effect {

  /** 効果処理を表現する関数 */
  type Effect = (Game, PlayerKey, MutableCard) => List[Process]

  /** 何も行わない */
  val DoNothing: Effect = (_, _, _) => Nil
}

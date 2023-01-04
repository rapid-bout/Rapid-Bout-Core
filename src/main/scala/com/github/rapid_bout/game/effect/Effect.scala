package com.github.rapid_bout.game.effect

import com.github.rapid_bout.game.effect.action.Process
import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

object Effect {
  type Effect = (Game, PlayerKey, MutableCard) => List[Process]
  val DoNothing: Effect = (_, _, _) => Nil
}

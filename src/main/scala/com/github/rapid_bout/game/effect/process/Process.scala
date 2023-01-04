package com.github.rapid_bout.game.effect.process

import cats.data.Writer
import com.github.rapid_bout.game.Game
import com.github.rapid_bout.history.History
sealed trait Process

object Process {

  trait Select extends Process

  trait Action extends Process {
    val name: String
    def apply(game: Game, args: List[Any]): Writer[Seq[History], Game]
  }

}

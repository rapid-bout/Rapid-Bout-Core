package com.github.rapid_bout.game.effect.process.action

import cats.data.Writer
import com.github.rapid_bout.game.Game
import com.github.rapid_bout.game.effect.process.Process.Action
import com.github.rapid_bout.history.History

case class ReturnOfStack() extends Action {
  override val name: String = "return_of_hand"
  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    game.returnOfStack(game.activePlayer)
  }
}

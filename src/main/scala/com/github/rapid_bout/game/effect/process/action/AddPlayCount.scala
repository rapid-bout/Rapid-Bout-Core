package com.github.rapid_bout.game.effect.process.action

import cats.data.Writer
import com.github.rapid_bout.game.Game
import com.github.rapid_bout.game.effect.process.Process.Action
import com.github.rapid_bout.history.History

case class AddPlayCount() extends Action {
  override val name: String = "add_play_count"
  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    game.addPlayCount()
  }
}

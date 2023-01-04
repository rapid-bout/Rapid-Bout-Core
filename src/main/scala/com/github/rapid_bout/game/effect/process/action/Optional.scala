package com.github.rapid_bout.game.effect.process.action

import cats.data.Writer
import com.github.rapid_bout.game.Game
import com.github.rapid_bout.game.effect.process.Process.Action
import com.github.rapid_bout.history.History

case class Optional(action: Action) extends Action {
  override val name: String = s"option: ${action.name}"
  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    args match {
      case true :: list => action.apply(game, list)
      case false :: _ => Writer.apply(Nil, game)
      case args => throw new IllegalArgumentException(args.mkString(","))
    }
  }
}

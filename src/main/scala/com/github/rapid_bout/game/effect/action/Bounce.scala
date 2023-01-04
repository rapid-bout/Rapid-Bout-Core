package com.github.rapid_bout.game.effect.action

import cats.data.Writer
import com.github.rapid_bout.game.{Game, PlayerKey}
import com.github.rapid_bout.history.History

import Process.Action

object Bounce extends Action {
  override val name: String = "bounce"
  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    args match {
      case (initiator: PlayerKey) :: (indexes: Seq[_]) :: Nil if indexes.forall(_.isInstanceOf[Int]) =>
        game.bounce(initiator, indexes.asInstanceOf[Seq[Int]])
      case args => throw new IllegalArgumentException(args.mkString(","))
    }
  }
}

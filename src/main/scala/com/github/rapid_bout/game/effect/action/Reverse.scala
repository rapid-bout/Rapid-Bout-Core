package com.github.rapid_bout.game.effect.action

import cats.data.Writer
import com.github.rapid_bout.game.{Game, PlayerKey}
import com.github.rapid_bout.history.History

import Process.Action

object Reverse extends Action {
  override val name: String = "reverse"

  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    args match {
      case (initiator: PlayerKey) :: (indexes: Seq[_]) :: (isReverse: Option[_]) :: Nil
          if indexes.forall(_.isInstanceOf[Int]) && isReverse.forall(_.isInstanceOf[Boolean]) =>
        game.reverse(initiator, indexes.asInstanceOf[Seq[Int]], isReverse.asInstanceOf[Option[Boolean]])
      case args => throw new IllegalArgumentException(args.mkString(","))
    }
  }
}

package com.github.rapid_bout.game.effect.action

import cats.data.Writer
import com.github.rapid_bout.game.{Game, PlayerKey}
import com.github.rapid_bout.history.History

import Process.Action

case class ReturnOfHand(initiator: PlayerKey) extends Action {
  override val name: String = "return_of_hand"
  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    args match {
      case (indexes: Seq[_]) :: Nil if indexes.forall(_.isInstanceOf[Int]) =>
        game.returnOfHand(initiator, indexes.asInstanceOf[Seq[Int]])
      case args => throw new IllegalArgumentException(args.mkString(","))
    }
  }
}

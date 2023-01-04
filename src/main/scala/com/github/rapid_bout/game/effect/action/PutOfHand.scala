package com.github.rapid_bout.game.effect.action

import cats.data.Writer
import com.github.rapid_bout.game.{Game, PlayerKey}
import com.github.rapid_bout.history.History

import Process.Action

case class PutOfHand(initiator: PlayerKey) extends Action {
  override val name: String = "put_of_hand"
  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    args match {
      case (indexes: Seq[_]) :: (reverse: Boolean) :: Nil if indexes.forall(_.isInstanceOf[Int]) =>
        game.putOfHand(initiator, indexes.asInstanceOf[Seq[Int]], reverse)
      case args => throw new IllegalArgumentException(args.mkString(","))
    }
  }
}

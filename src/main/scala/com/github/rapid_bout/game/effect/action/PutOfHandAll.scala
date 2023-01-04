package com.github.rapid_bout.game.effect.action

import cats.data.Writer
import com.github.rapid_bout.game.{Game, PlayerKey}
import com.github.rapid_bout.history.History

import Process.Action

case class PutOfHandAll(initiator: PlayerKey) extends Action {
  override val name: String = "put_of_hand_all"
  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    args match {
      case (reverse: Boolean) :: Nil =>
        game.putOfHandAll(initiator, reverse)
      case args => throw new IllegalArgumentException(args.mkString(","))
    }
  }
}

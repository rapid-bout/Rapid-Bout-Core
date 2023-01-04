package com.github.rapid_bout.game.effect.action

import cats.data.Writer
import com.github.rapid_bout.game.{Game, PlayerKey}
import com.github.rapid_bout.history.History

import Process.Action

case class ReverseAllWithFront(initiator: PlayerKey) extends Action {
  override val name: String = "reverse_all"
  override def apply(game: Game, args: List[Any]): Writer[Seq[History], Game] = {
    game.reverseAllWithFront(initiator)
  }
}

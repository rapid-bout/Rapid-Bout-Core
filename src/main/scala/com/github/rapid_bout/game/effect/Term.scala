package com.github.rapid_bout.game.effect

import com.github.rapid_bout.game.{Game, PlayerKey}

trait Term {
  def verify(game: Game, initiator: PlayerKey): Boolean
}

object Term {
  case object AnyTime extends Term {
    override def verify(game: Game, initiator: PlayerKey): Boolean = true
  }
  case class MoreHand(count: Int) extends Term {
    override def verify(game: Game, initiator: PlayerKey): Boolean =
      game.get(initiator).hand.count >= count
  }
  case object Forbidden extends Term {
    def verify(game: Game, initiator: PlayerKey): Boolean = false
  }
}

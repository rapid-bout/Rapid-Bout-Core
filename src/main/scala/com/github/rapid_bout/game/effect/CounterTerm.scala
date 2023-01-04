package com.github.rapid_bout.game.effect

import com.github.rapid_bout.game.{Game, MutableCard, PlayerKey}

trait CounterTerm {
  def verify(game: Game, initiator: PlayerKey, willPlay: MutableCard): Boolean
}

object CounterTerm {
  case object Forbidden extends CounterTerm {
    def verify(game: Game, initiator: PlayerKey, willPlay: MutableCard): Boolean = false
  }
}

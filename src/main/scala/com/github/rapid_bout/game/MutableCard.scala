package com.github.rapid_bout.game

import java.util.UUID

import com.github.rapid_bout.game.card.Card
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.{CounterTerm, Term}

case class MutableCard(card: Card) {
  val uuid: UUID = java.util.UUID.randomUUID()
  val point: Int = card.point
  val term: Term = card.term
  val counterTerm: CounterTerm = card.counterTerm
  val preprocess: Effect = card.preprocess
  val effect: Effect = card.effect
}

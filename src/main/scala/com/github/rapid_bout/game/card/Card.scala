package com.github.rapid_bout.game.card
import com.github.rapid_bout.game.effect.CounterTerm.Forbidden
import com.github.rapid_bout.game.effect.Effect.{DoNothing, Effect}
import com.github.rapid_bout.game.effect.Term.AnyTime
import com.github.rapid_bout.game.effect.{CounterTerm, Term}

trait Card {
  val point: Int
  val term: Term = AnyTime
  val counterTerm: CounterTerm = Forbidden
  val preprocess: Effect = DoNothing
  val effect: Effect = DoNothing
}

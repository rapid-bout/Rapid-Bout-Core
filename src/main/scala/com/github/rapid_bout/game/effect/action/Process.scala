package com.github.rapid_bout.game.effect.action

import cats.data.Writer
import com.github.rapid_bout.game.{Game, PlayerKey, Zone}
import com.github.rapid_bout.history.History

sealed trait Process

object Process {

  sealed trait Select extends Process

  case class BoolSelect(selector: PlayerKey) extends Select

  case class UserSelect(
      selector: PlayerKey,
      target: PlayerKey,
      zone: Zone,
      number: Int,
      opt: Boolean,
      duplicate: Boolean
  ) extends Select

  case class RandomSelect(
      selector: PlayerKey,
      target: PlayerKey,
      zone: Zone,
      number: Int,
      duplicate: Boolean
  ) extends Select

  case class Const(
      value: Any
  ) extends Select

  trait Action extends Process {
    val name: String
    def apply(game: Game, args: List[Any]): Writer[Seq[History], Game]
  }

}

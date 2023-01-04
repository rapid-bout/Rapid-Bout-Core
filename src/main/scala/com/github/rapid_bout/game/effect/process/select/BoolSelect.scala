package com.github.rapid_bout.game.effect.process.select

import com.github.rapid_bout.game.PlayerKey
import com.github.rapid_bout.game.effect.process.Process.Select

case class BoolSelect(selector: PlayerKey) extends Select

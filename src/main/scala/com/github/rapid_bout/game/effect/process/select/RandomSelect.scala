package com.github.rapid_bout.game.effect.process.select

import com.github.rapid_bout.game.effect.process.Process.Select
import com.github.rapid_bout.game.{PlayerKey, Zone}

case class RandomSelect(
    selector: PlayerKey,
    target: PlayerKey,
    zone: Zone,
    number: Int,
    duplicate: Boolean
) extends Select

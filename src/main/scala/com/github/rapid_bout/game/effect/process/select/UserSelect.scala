package com.github.rapid_bout.game.effect.process.select

import com.github.rapid_bout.game.effect.process.Process.Select
import com.github.rapid_bout.game.{PlayerKey, Zone}

case class UserSelect(
    selector: PlayerKey,
    target: PlayerKey,
    zone: Zone,
    number: Int,
    opt: Boolean,
    duplicate: Boolean
) extends Select

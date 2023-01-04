package helper

import scala.language.implicitConversions

import com.github.rapid_bout.game.MutableCard
import com.github.rapid_bout.game.card.Card

object ToMutableCard {
  implicit def ToMutableCard(card: Card): MutableCard = {
    MutableCard(card)
  }
}

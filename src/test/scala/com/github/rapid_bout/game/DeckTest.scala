package com.github.rapid_bout.game

import scala.util.Random

import com.github.rapid_bout.game.card.{_00001, _00002, _00011}
import com.github.rapid_bout.util.Exceptions.{IllegalDeckNumberException, IllegalDuplicateCardException}
import helper.DeckTemplate
import helper.ToMutableCard.ToMutableCard
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

class DeckTest extends AnyFunSuite with BeforeAndAfter {
  private val randomMock = Mockito.mock(classOf[Random])

  before {
    // player1 の先行
    Mockito.doReturn(0).when(randomMock).between(ArgumentMatchers.any(), ArgumentMatchers.any())
  }

  after {
    Mockito.reset(randomMock)
  }

  private def create(): Deck = {
    Deck(DeckTemplate.DeckList.map(MutableCard))
  }

  test("デッキ枚数が20枚以上なら IllegalDeckNumberException") {
    intercept[IllegalDeckNumberException] {
      Deck((DeckTemplate.DeckList :+ _00011).map(MutableCard))
    }
  }

  test("デッキに同じカードを2枚以上入れていたら IllegalDuplicateCardException") {
    intercept[IllegalDuplicateCardException] {
      Deck((0 until 20).map(_ => _00001).map(MutableCard))
    }
  }

  test("draw: カードを0枚より少なく引くことはできない") {
    intercept[IllegalArgumentException] {
      create().draw(-1)
    }
  }

  test("draw: カードを1枚引く") {
    val deck = create()
    deck.draw(1) match {
      case (actual, Seq(card)) =>
        assert(actual.decks == deck.decks.tail)
        assert(card.card == _00001)
      case _ => fail()
    }
  }

  test("draw: カードを複数枚引く") {
    val deck = create()
    deck.draw(2) match {
      case (actual, Seq(card1, card2)) =>
        assert(actual.decks == deck.decks.tail.tail)
        assert(card1.card == _00001)
        assert(card2.card == _00001)
      case _ => fail()
    }
  }

  test("append: カードを1枚追加できる") {
    val deck = create()
    assert(deck.append(_00011).decks == deck.decks :+ MutableCard(_00011))
  }

  test("appendAll: カードを複数枚追加できる") {
    val deck = create()
    assert(
      deck.appendAll(Seq(_00011, _00011)).decks == (deck.decks ++ Seq[MutableCard](_00011, _00011))
    )
  }

  test("removeAll: カードを複数枚上から順番に取り出す") {
    val deck = create()
    deck.removeAll(Seq(0, 1, 2, 3)) match {
      case (actual, cards) =>
        assert(cards.map(_.card) == Seq(_00001, _00001, _00002, _00002))
        assert(actual.decks.map(_.card) == deck.decks.tail.tail.tail.tail.map(_.card))
    }
  }

  test("shuffle: 山札をシャッフルする") {
    Mockito.doReturn(DeckTemplate.DeckList.reverse.map(MutableCard)).when(randomMock)
      .shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())
    val deck = create()
    assert(deck.shuffle()(randomMock).decks.map(_.card) == deck.decks.map(_.card).reverse)
  }
}

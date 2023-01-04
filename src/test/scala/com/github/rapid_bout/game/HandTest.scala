package com.github.rapid_bout.game

import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

import com.github.rapid_bout.game.card.{_00001, _00002, _00003, _00004, _00007}
import helper.ToMutableCard.ToMutableCard
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

class HandTest extends AnyFunSuite with BeforeAndAfter {
  private def create(): Hand = Hand()

  private val game = Mockito.mock(classOf[Game])
  private val player = Mockito.mock(classOf[Player])

  private val randomMock = Mockito.mock(classOf[Random])

  after {
    Mockito.reset(randomMock)
    Mockito.reset(game)
    Mockito.reset(player)
  }

  test("couterable: 非カウンターカード") {
    create() pipe { hand =>
      hand.append(_00001)
    } pipe { hand =>
      assert(!hand.counterable(
        game,
        index = 0,
        willPlay = _00001
      ))
    }
  }

  test("count: 手札0枚") {
    assert(create().count == 0)
  }

  test("append: 手札にカードを1枚追加する") {
    create() pipe { hand =>
      hand.append(_00001)
    } pipe { hand =>
      hand.hands match {
        case Seq(card) =>
          assert(card.card == _00001)
          assert(hand.count == 1)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("append: 手札に複数回カードが追加できる") {
    create() pipe { hand =>
      hand.append(_00001).append(_00002)
    } pipe { hand =>
      hand.hands match {
        case Seq(card1, card2) =>
          assert(_00001 == card1.card)
          assert(_00002 == card2.card)
          assert(hand.count == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("appendAll: 手札に複数枚カードが追加できる") {
    create() pipe { hand =>
      hand.appendAll(Seq(_00001, _00002))
    } pipe { hand =>
      hand.hands match {
        case Seq(card1, card2) =>
          assert(_00001 == card1.card)
          assert(_00002 == card2.card)
          assert(hand.count == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("remove: 手札の先頭のカードを取り出す") {
    create() pipe { hand =>
      hand.appendAll(Seq(_00001, _00002))
    } pipe { hand =>
      hand.remove(0)
    } pipe { case (hand, card) =>
      assert(card.card == _00001)
      hand.hands match {
        case Seq(card) =>
          assert(_00002 == card.card)
          assert(hand.count == 1)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("remove: 手札の先頭以外のカードを取り出す") {
    create() pipe { hand =>
      hand.appendAll(Seq(_00001, _00002))
    } pipe { hand =>
      hand.remove(1)
    } pipe { case (hand, card) =>
      assert(card.card == _00002)
      hand.hands match {
        case Seq(card) =>
          assert(_00001 == card.card)
          assert(hand.count == 1)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("getRandomIndex: 重複なし") {
    val expected = Seq(0, 1, 2, 3)
    Mockito.doReturn(Seq(0, 1, 2, 3)).when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())
    create() pipe { hand =>
      assert(hand.getRandomIndex(4, duplicate = false)(randomMock) == expected)
    }
  }

  test("getRandomIndex: 重複あり") {
    val expected = Seq(0, 1, 2, 3)
    Mockito.doReturn(expected.head, expected.tail: _*).when(randomMock).between(
      ArgumentMatchers.any(),
      ArgumentMatchers.any()
    )
    create() pipe { hand =>
      assert(hand.getRandomIndex(4, duplicate = true)(randomMock) == expected)
    }
  }

  test("playable: 条件なし") {
    create() pipe { hand =>
      hand.append(_00001)
    } pipe { hand =>
      assert(hand.playable(game, 0))
    }
  }

  test("playable: 条件を満たす") {
    create() pipe { hand =>
      hand.appendAll(Seq(_00001, _00007))
    } pipe { hand =>
      Mockito.doReturn(hand).when(player).hand
      Mockito.doReturn(player).when(game).get(ArgumentMatchers.any())
      assert(hand.playable(game, 1))
    }
  }

  test("playable: 条件を満たさない") {
    create() pipe { hand =>
      hand.append(_00007)
    } pipe { hand =>
      Mockito.doReturn(hand).when(player).hand
      Mockito.doReturn(player).when(game).get(ArgumentMatchers.any())
      assert(!hand.playable(game, 0))
    }
  }

  test("removeAll: 手札のカードを全て取り出す") {
    create() pipe { hand =>
      hand.appendAll(Seq(_00001, _00002))
    } pipe { hand =>
      hand.removeAll(Seq(0, 1))
    } pipe { case (hand, cards) =>
      assert(cards.map(_.card) == Seq(_00001, _00002))
      hand.hands match {
        case Seq() =>
          assert(hand.count == 0)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("removeAll: 手札のカードを一部取り出す") {
    create() pipe { hand =>
      hand.appendAll(Seq(_00001, _00002, _00003, _00004))
    } pipe { hand =>
      hand.removeAll(Seq(1, 2))
    } pipe { case (hand, cards) =>
      assert(cards.map(_.card) == Seq(_00002, _00003))
      hand.hands match {
        case Seq(card1, card2) =>
          assert(card1.card == _00001)
          assert(card2.card == _00004)
          assert(hand.count == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

}

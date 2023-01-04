package com.github.rapid_bout.game

import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

import com.github.rapid_bout.game.card.{_00001, _00002, _00006}
import com.github.rapid_bout.game.effect.Effect.DoNothing
import com.github.rapid_bout.util.ExSeq.SwapSeq
import com.github.rapid_bout.util.Exceptions.StackIsEmptyException
import helper.DeckTemplate
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

import PlayerKey.PlayerKeyForUser

class PlayerTest extends AnyFunSuite with BeforeAndAfter {

  private val game = Mockito.mock(classOf[Game])
  private val player = Mockito.mock(classOf[Player])

  private val randomMock = Mockito.mock(classOf[Random])

  after {
    Mockito.reset(randomMock)
    Mockito.reset(game)
    Mockito.reset(player)
  }

  private def create: Player = Player(
    PlayerKeyForUser("1"),
    DeckTemplate.DeckList.map(MutableCard)
  )(randomMock)

  test("shuffle: 山札をシャッフルする") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0).reverse
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.shuffle()(randomMock)
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.reverse)
    }
  }

  test("draw: カードを1枚引く") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      assert(player.hand.hands.map(_.card) == Seq(_00001))
    }
  }

  test("draw: カードを複数枚引く") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(3)
    } pipe { player =>
      assert(player.hand.hands.map(_.card) == Seq(_00001, _00001, _00002))
    }
  }

  test("returnOfHand: カードを1枚手札から山札に戻す") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.returnOfHand(Seq(0))
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail :+ DeckTemplate.DeckList.head)
      assert(player.hand.hands.map(_.card) == Seq.empty)
    }
  }

  test("returnOfHand: カードを複数枚手札から山札に戻す") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(3)
    } pipe { player =>
      player.returnOfHand(Seq(0, 1))
    } pipe { player =>
      assert(player.deck.decks.map(
        _.card
      ) == DeckTemplate.DeckList.tail.tail.tail :+ DeckTemplate.DeckList.head :+ DeckTemplate.DeckList(1))
      assert(player.hand.hands.map(_.card) == Seq(_00002))
    }
  }

  test("putOfHand: カードを1枚手札から場に裏向きで置く") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.putOfHand(Seq(0), reverse = true)
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      player.field.fields match {
        case Seq((card, Side.Back)) =>
          assert(card.card == _00001)
        case _ => fail()
      }
      assert(player.count() == 2)
    }
  }

  test("putOfHand: カードを1枚手札から場に表向きで置く") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.putOfHand(Seq(0), reverse = false)
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      player.field.fields match {
        case Seq((card, Side.Front)) =>
          assert(card.card == _00001)
        case _ => fail()
      }
      assert(player.count() == _00001.point)
    }
  }

  test("putOfHand: カードを複数枚手札から場に裏向きで置く") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(3)
    } pipe { player =>
      player.putOfHand(Seq(0, 1), reverse = true)
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail.tail.tail)
      assert(player.hand.hands.map(_.card) == Seq(_00002))
      player.field.fields match {
        case Seq((card1, Side.Back), (card2, Side.Back)) =>
          assert(card1.card == _00001)
          assert(card2.card == _00001)
        case _ => fail()
      }
      assert(player.count() == 4)
    }
  }

  test("putOfDeck: カードを1枚山札から場に裏向きで置く") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.putOfDeck(Seq(0), reverse = true)
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      player.field.fields match {
        case Seq((card, Side.Back)) =>
          assert(card.card == _00001)
        case _ => fail()
      }
      assert(player.count() == 2)
    }
  }

  test("putOfHand: カードを1枚山札から場に表向きで置く") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.putOfDeck(Seq(0), reverse = false)
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      player.field.fields match {
        case Seq((card, Side.Front)) =>
          assert(card.card == _00001)
        case _ => fail()
      }
      assert(player.count() == _00001.point)
    }
  }

  test("putOfHand: カードを複数枚山札から場に裏向きで置く") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.putOfDeck(Seq(0, 1), reverse = true)
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail.tail)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      player.field.fields match {
        case Seq((card1, Side.Back), (card2, Side.Back)) =>
          assert(card1.card == _00001)
          assert(card2.card == _00001)
        case _ => fail()
      }
      assert(player.count() == 4)
    }
  }

  test("playable: 条件なし") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      assert(player.playable(game, 0))
    }
  }

  test("stack: 手札のカードを裏向きでスタックに積むことができる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.stack(0, reverse = true)
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      player.stack match {
        case Some((card, Side.Back)) =>
          assert(card.card == _00001)
        case _ => fail()
      }
      assert(player.field.fields == Seq.empty)
    }
  }

  test("stack: 手札のカードを表向きでスタックに積むことができる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.stack(0, reverse = false)
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      player.stack match {
        case Some((card, Side.Front)) =>
          assert(card.card == _00001)
        case _ => fail()
      }
      assert(player.field.fields == Seq.empty)
    }
  }

  test("returnOfStack: スタックにカードが積まれていない場合、StackIsEmptyException") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      intercept[StackIsEmptyException] {
        player.returnOfStack()
      }
    }
  }

  test("returnOfStack: スタック上に乗っているカードを山札に戻す") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.stack(0, reverse = false)
    } pipe { player =>
      player.returnOfStack()
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail :+ DeckTemplate.DeckList.head)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      assert(player.stack.isEmpty)
      assert(player.field.fields == Seq.empty)
    }
  }

  test("play: スタックにカードが積まれていない場合、StackIsEmptyException") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      intercept[StackIsEmptyException] {
        player.play()
      }
    }
  }

  test("play: スタックの表向きのカードを場に出すことができる (効果なし)") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.stack(0, reverse = false)
    } pipe { player =>
      player.play() match {
        case (player, effect) =>
          assert(effect == DoNothing)
          assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail)
          assert(player.hand.hands.map(_.card) == Seq.empty)
          assert(player.stack.isEmpty)
          player.field.fields match {
            case Seq((card, Side.Front)) =>
              assert(card.card == _00001)
            case _ => fail()
          }
          assert(player.count() == _00001.point)
      }
    }
  }

  test("play: スタックの表向きのカードを場に出すことができる (効果あり)") {
    // _00006 (効果ありカード) がトップに来るように固定する
    Mockito.doAnswer { invocation =>
      val decks = invocation.getArgument[Seq[MutableCard]](0)
      decks.swap(0, decks.indexWhere(_.card == _00006))
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.stack(0, reverse = false)
    } pipe { player =>
      player.play() match {
        case (player, effect) =>
          assert(effect == _00006.effect)
          assert(player.deck.decks.map(_.card) ==
            DeckTemplate.DeckList.swap(0, DeckTemplate.DeckList.indexWhere(_ == _00006)).tail)
          assert(player.hand.hands.map(_.card) == Seq.empty)
          assert(player.stack.isEmpty)
          player.field.fields match {
            case Seq((card, Side.Front)) =>
              assert(card.card == _00006)
            case _ => fail()
          }
          assert(player.count() == _00006.point)
      }
    }
  }

  test("play: スタックの裏向きのカードを場に出すことができる") {
    // _00006 (効果ありカード) がトップに来るように固定する
    Mockito.doAnswer { invocation =>
      val decks = invocation.getArgument[Seq[MutableCard]](0)
      decks.swap(0, decks.indexWhere(_.card == _00006))
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.stack(0, reverse = true)
    } pipe { player =>
      player.play() match {
        case (player, effect) =>
          assert(effect == DoNothing)
          assert(
            player.deck.decks.map(_.card) ==
              DeckTemplate.DeckList.swap(0, DeckTemplate.DeckList.indexWhere(_ == _00006)).tail
          )
          assert(player.hand.hands.map(_.card) == Seq.empty)
          assert(player.stack.isEmpty)
          player.field.fields match {
            case Seq((card, Side.Back)) =>
              assert(card.card == _00006)
            case _ => fail()
          }
          assert(player.count() == 2)
      }
    }
  }

  test("bounce: 場のカードを手札に戻すことができる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.stack(0, reverse = false)
    } pipe { player =>
      player.play()
    } pipe { case (player, _) =>
      player.bounce(Seq(0))
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail)
      assert(player.hand.hands.map(_.card) == Seq(_00001))
      assert(player.stack.isEmpty)
      assert(player.field.fields == Seq.empty)
    }
  }

  test("returnOfField: 場のカードを山札に戻すことができる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    create pipe { player =>
      player.draw(1)
    } pipe { player =>
      player.stack(0, reverse = false)
    } pipe { player =>
      player.play()
    } pipe { case (player, _) =>
      player.returnOfField(Seq(0))
    } pipe { player =>
      assert(player.deck.decks.map(_.card) == DeckTemplate.DeckList.tail :+ DeckTemplate.DeckList.head)
      assert(player.hand.hands.map(_.card) == Seq.empty)
      assert(player.stack.isEmpty)
      assert(player.field.fields == Seq.empty)
    }
  }

}

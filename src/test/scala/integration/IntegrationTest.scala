package integration

import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

import cats.data.Writer
import com.github.rapid_bout.game
import com.github.rapid_bout.game.PlayerKey.{Both, PlayerKeyForUser}
import com.github.rapid_bout.game.card._
import com.github.rapid_bout.game.effect.action.Bounce
import com.github.rapid_bout.game.effect.action.Process.UserSelect
import com.github.rapid_bout.game.{Game, MutableCard, Side, Zone}
import com.github.rapid_bout.history.{Finish, History, Move}
import com.github.rapid_bout.util.ExSeq.SwapSeq
import helper.DeckTemplate
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.PrivateMethodTester._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfter, PrivateMethodTester}

class IntegrationTest extends AnyFunSuite with BeforeAndAfter {
  private val randomMock = Mockito.mock(classOf[Random])

  before {
    // player1 の先行
    Mockito.doReturn(0).when(randomMock).between(ArgumentMatchers.any(), ArgumentMatchers.any())
  }

  after {
    Mockito.reset(randomMock)
  }

  private val player1 = (
    PlayerKeyForUser("player1"),
    DeckTemplate.DeckList
  )

  private val player2 = (
    PlayerKeyForUser("player2"),
    DeckTemplate.DeckList
  )

  private def createGame(deck: Seq[Card] = player1._2): Game = {
    game.Game(
      player1 = (player1._1, deck),
      player2 = player2
    )(randomMock)
  }

  test("最初のターンを開始することができる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    assert(game.get(player1._1).hand.hands.map(_.card) == Seq(_00001, _00001, _00002, _00002))
    assert(game.get(player2._1).hand.hands.map(_.card) == Seq(_00001, _00001, _00002))
    assert(game.playCount == 1)
    assert(game.turn == 1)
    assert(game.activePlayer == player1._1)
    assert(game.nonActivePlayer == player2._1)
  }

  test("カードを裏向きでスタックに積むことができる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    game.preprocess(0, reverse = true).run match {
      case (history, (game, None)) =>
        assert(history == List(Move(player1._1, List(0), (Zone.Hand, Zone.Stack), Some(true))))
        val player = game.get(game.activePlayer)
        assert(player.hand.hands.map(_.card) == Seq(_00001, _00002, _00002))
        player.stack match {
          case Some((card, Side.Back)) => assert(card.card == _00001)
          case None => fail(s"スタックにカードが積まれていない")
          case _ => fail()
        }
      case _ => fail("効果が発動している")
    }
  }

  test("カードを裏向きでプレイできる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    game.preprocess(0, reverse = true).run match {
      case (_, (game, None)) =>
        game.play().run match {
          case (history, (game, None)) =>
            assert(history == List(Move(player1._1, List(0), (Zone.Stack, Zone.Field), Some(true))))
            game.get(game.activePlayer).field.fields match {
              case Seq((card, Side.Back)) if card.card == _00001 =>
              case _ => fail("フィールドの状態が不正です")
            }
          case _ => fail()
        }
      case _ => fail("効果が発動している")
    }
  }

  test("効果のないカードをスタックに積むことができる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    game.preprocess(0, reverse = false).run match {
      case (history, (game, None)) =>
        assert(history == List(Move(player1._1, List(0), (Zone.Hand, Zone.Stack), Some(false))))
        val player = game.get(game.activePlayer)
        assert(player.hand.hands.map(_.card) == Seq(_00001, _00002, _00002))
        player.stack match {
          case Some((card, Side.Front)) => assert(card.card == _00001)
          case None => fail(s"スタックにカードが積まれていない")
          case _ => fail()
        }
      case _ => fail("効果が発動している")
    }
  }

  test("効果のないカードをプレイできる") {
    // シャッフルは行わない
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    game.preprocess(0, reverse = false).run match {
      case (_, (game, None)) =>
        game.play().run match {
          case (history, (game, None)) =>
            assert(history == List(Move(player1._1, List(0), (Zone.Stack, Zone.Field), Some(false))))
            game.get(game.activePlayer).field.fields match {
              case Seq((card, Side.Front)) if card.card == _00001 =>
              case _ => fail("フィールドの状態が不正です")
            }
          case _ => fail()
        }
      case _ => fail("効果が発動している")
    }
  }

  test("カードのプレイ条件を満たさない場合、IllegalArgumentException") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      // 初手に _00003 を引くように山札操作
      val index = deck.indexOf(MutableCard(_00003))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame() pipe { game =>
      val returnOfHand = PrivateMethodTester.PrivateMethod[Writer[Seq[History], Game]](Symbol("returnOfHand"))
      // 手札が1枚だけになるようにする
      (game invokePrivate returnOfHand(game.activePlayer, Seq(1, 2, 3))).run._2
    }
    assert(game.get(game.activePlayer).hand.hands.map(_.card) == Seq(_00003))
    intercept[IllegalArgumentException] {
      game.preprocess(0, reverse = false)
    }
  }

  test("スタックに積む際の条件処理が積まれる") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      // 初手に _00003 を引くように山札操作
      val index = deck.indexOf(MutableCard(_00003))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    game.preprocess(0, reverse = false).run match {
      case (history, (game, Some(select))) =>
        assert(history == List(Move(player1._1, List(0), (Zone.Hand, Zone.Stack), Some(false))))
        assert(select == UserSelect(player1._1, player1._1, Zone.Hand, number = 1, opt = false, duplicate = false))
        game.get(game.activePlayer).stack match {
          case Some((card, Side.Front)) => assert(card.card == _00003)
          case None => fail(s"スタックにカードが積まれていない")
          case _ => fail()
        }
      case _ => fail("条件処理が取得できない")
    }
  }

  test("条件処理を解決できる") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      // 初手に _00003 を引くように山札操作
      val index = deck.indexOf(MutableCard(_00003))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    (game.preprocess(0, reverse = false).run._2 match {
      case (game, Some(_)) => game.select(indexes = Seq(0))
      case (_, None) => fail("ユーザー選択が積まれていない")
    }).run match {
      case (history, (game, None)) =>
        assert(history == List(Move(player1._1, List(0), (Zone.Hand, Zone.Deck), None)))
        val player = game.get(game.activePlayer)
        assert(game.effectStack == Nil)
        assert(player.hand.hands.map(_.card) == List(_00002, _00002))
        player.stack match {
          case Some((card, Side.Front)) => assert(card.card == _00003)
          case _ => fail("スタックにカードが積まれていない")
        }
      case (_, (_, Some(select))) => fail(s"ユーザー選択が解決されていない $select")
    }
  }

  test("条件処理解決後のカードを場に出すことができる (効果なし)") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      // 初手に _00003 を引くように山札操作
      val index = deck.indexOf(MutableCard(_00003))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    ((game.preprocess(0, reverse = false).run._2 match {
      case (game, Some(_)) => game.select(indexes = Seq(0))
      case (_, None) => fail("ユーザー選択が積まれていない")
    }).run._2 match {
      case (game, None) => game.play()
      case (_, Some(select)) => fail(s"効果の解決処理が完了していない $select")
    }).run match {
      case (history, (game, None)) =>
        assert(history == List(Move(player1._1, List(0), (Zone.Stack, Zone.Field), Some(false))))
        val player = game.get(game.activePlayer)
        assert(game.effectStack == Nil)
        game.get(game.activePlayer).field.fields match {
          case Seq((card, Side.Front)) if card.card == _00003 =>
          case _ => fail("フィールドの状態が不正です")
        }
        assert(player.stack.isEmpty)
      case (_, (_, Some(select))) => fail(s"登場時の効果処理は存在しないはず $select")
    }
  }

  test("場にカードを出した際の効果が処理される") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      val index = deck.indexOf(MutableCard(_00006))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    (game.preprocess(0, reverse = false).run._2 match {
      case (game, None) => game.play()
      case (_, Some(_)) => fail("発動条件処理が存在している")
    }).run match {
      case (history, (game, None)) =>
        assert(
          history == List(
            Move(player1._1, List(0), (Zone.Stack, Zone.Field), Some(false)),
            Move(player1._1, List(0, 1), (Zone.Deck, Zone.Hand), None)
          )
        )
        val player = game.get(game.activePlayer)
        assert(game.effectStack == Nil)
        game.get(game.activePlayer).field.fields match {
          case Seq((card, Side.Front)) if card.card == _00006 =>
          case _ => fail("フィールドの状態が不正です")
        }
        assert(player.hand.hands.map(_.card) == List(_00001, _00002, _00002, _00003, _00003))
        assert(player.stack.isEmpty)
      case (_, (_, Some(select))) => fail(s"登場時の効果処理は存在しないはず $select")
    }
  }

  test("場にカードを出した際の効果で選択処理が積まれる") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      val index = deck.indexOf(MutableCard(_00010))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    (game.preprocess(0, reverse = false).run._2 match {
      case (game, None) => game.play()
      case (_, Some(_)) => fail("発動条件処理が存在している")
    }).run._2 match {
      case (game, Some(select)) =>
        assert(select == UserSelect(player1._1, Both, Zone.Field, number = 1, opt = false, duplicate = false))
        val player = game.get(game.activePlayer)
        assert(
          game.effectStack == List(
            UserSelect(player1._1, Both, Zone.Field, number = 1, opt = false, duplicate = false),
            Bounce
          )
        )
        game.get(game.activePlayer).field.fields match {
          case Seq((card, Side.Front)) if card.card == _00010 =>
          case _ => fail("フィールドの状態が不正です")
        }
        assert(player.stack.isEmpty)
      case (_, None) => fail(s"登場時の効果処理が積まれていない")
    }
  }

  test("場にカードを出した際の効果処理を解決できる") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      val index = deck.indexOf(MutableCard(_00010))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    ((game.preprocess(0, reverse = false).run._2 match {
      case (game, None) => game.play()
      case (_, Some(_)) => fail("発動条件処理が存在している")
    }).run._2 match {
      // _00010 を場に出した後、効果でそのまま手札に戻す
      case (game, Some(_)) => game.select(game.activePlayer, Seq(0))
      case (_, None) => fail(s"登場時の効果処理が積まれていない")
    }).run match {
      case (history, (game, None)) =>
        assert(history == List(Move(player1._1, List(0), (Zone.Field, Zone.Hand), None)))
        val player = game.get(game.activePlayer)
        assert(game.effectStack.isEmpty)
        assert(player.field.fields == Nil)
        assert(player.hand.hands.map(_.card) == List(_00001, _00002, _00002, _00010))
        assert(player.stack.isEmpty)
      case (_, (_, Some(select))) => fail(s"効果処理解決が完了していない $select")
    }
  }

  test("最初のターンはカードを1枚しかプレイできない") {
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    game.preprocess(0, reverse = false).run match {
      case (_, (game, None)) =>
        game.play().run._2 match {
          case (game, None) =>
            intercept[IllegalArgumentException] {
              game.preprocess(0, reverse = false)
            }
          case _ => fail()
        }
      case _ => fail("効果が発動している")
    }
  }

  test("ターン開始時の処理が行える") {
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    game.turnEnd().run match {
      case (history, game) =>
        assert(history == List(Move(player2._1, 0 until 1, (Zone.Deck, Zone.Hand), None)))
        assert(game.activePlayer == player2._1)
        assert(game.nonActivePlayer == player1._1)
        assert(game.turn == 2)
        assert(game.playCount == 2)
        assert(game.get(game.activePlayer).hand.hands.map(_.card) == List(_00001, _00001, _00002, _00002))
    }
  }

  test("条件処理中はターンを終了できない") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      val index = deck.indexOf(MutableCard(_00003))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    game.preprocess(0, reverse = false).run match {
      case (_, (game, Some(_))) =>
        intercept[IllegalArgumentException] {
          game.turnEnd()
        }
      case _ => fail("条件処理が取得できない")
    }
  }

  test("カードを裏向きで 10枚出したらゲームが (強制的に) 終わる") {
    Mockito.doAnswer { invocation =>
      invocation.getArgument[Seq[MutableCard]](0)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    val last = (0 until 9).foldLeft(Writer(Seq.empty[History], game)) { (writer, _) =>
      writer flatMap { game =>
        game.preprocess(0, reverse = true)
      } flatMap {
        case (game, _) => game.play()
      } flatMap {
        case (game, _) => game.turnEnd()
      } flatMap { game =>
        game.turnEnd()
      }
    }.run._2
    (last.preprocess(0, reverse = true) flatMap {
      case (game, _) => game.play()
    }).run match {
      case (history, (game, _)) =>
        assert(history == List(
          Move(player1._1, List(0), (Zone.Hand, Zone.Stack), Some(true)),
          Move(player1._1, List(0), (Zone.Stack, Zone.Field), Some(true)),
          Finish(player1._1)
        ))
        assert(game.finish)
        assert(game.get(game.activePlayer).count() == 20)
    }
  }

  test("ゲームが終了している場合、効果処理を行わない") {
    Mockito.doAnswer { invocation =>
      val deck = invocation.getArgument[Seq[MutableCard]](0)
      val index = deck.indexOf(MutableCard(_00005))
      deck.swap(0, index)
    }.when(randomMock).shuffle(ArgumentMatchers.any())(ArgumentMatchers.any())

    val game = createGame()
    val last = (0 until 9).foldLeft(Writer(Seq.empty[History], game)) { (writer, _) =>
      writer flatMap { game =>
        game.preprocess(1, reverse = true)
      } flatMap {
        case (game, _) => game.play()
      } flatMap {
        case (game, _) => game.turnEnd()
      } flatMap { game =>
        game.turnEnd()
      }
    }.run._2
    (last.preprocess(0, reverse = false) flatMap {
      case (game, _) => game.play()
    }).run match {
      case (history, (game, _)) =>
        assert(history == List(
          Move(player1._1, List(0), (Zone.Hand, Zone.Stack), Some(false)),
          Move(player1._1, List(0), (Zone.Stack, Zone.Field), Some(false)),
          Finish(player1._1)
        ))
        assert(game.finish)
        val player = game.get(game.activePlayer)
        assert(player.count() == 20)
        // _0005 の効果でドローしていた場合は手札が 4枚になる
        assert(player.hand.hands.map(_.card) == List(_00006, _00006, _00007))
    }
  }

}

package com.github.rapid_bout.game

import scala.util.chaining.scalaUtilChainingOps

import com.github.rapid_bout.game.card.{_00001, _00002}
import helper.ToMutableCard.ToMutableCard
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

class FieldTest extends AnyFunSuite with BeforeAndAfter {
  private def create(): Field = Field()

  test("count: カードがない場合の打点は0") {
    val field = create()
    assert(field.count() == 0)
  }

  test("append: フィールドに裏向きでカードを1枚追加する") {
    create() pipe { field =>
      field.append(_00001, Side.Back)
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Back)) =>
          assert(_00001 == card.card)
          // 裏向きのカードは2点
          assert(field.count() == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("append: フィールドに表向きでカードを1枚追加する") {
    create() pipe { field =>
      field.append(_00001, Side.Front)
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Front)) =>
          assert(_00001 == card.card)
          assert(field.count() == _00001.point)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("append: フィールドに複数回カードが追加できる") {
    create() pipe { field =>
      field.append(_00001, Side.Front).append(_00002, Side.Back)
    } pipe { field =>
      field.fields match {
        case Seq((card1, Side.Front), (card2, Side.Back)) =>
          assert(_00001 == card1.card)
          assert(_00002 == card2.card)
          assert(field.count() == _00001.point + 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverse: 裏向きのカードを表向きにする") {
    create() pipe { field =>
      field.append(_00001, Side.Back)
    } pipe { field =>
      field.reverse(Seq(0), isReverse = None)
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Front)) =>
          assert(_00001 == card.card)
          assert(field.count() == _00001.point)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverse: 表向きのカードを裏向きにする") {
    create() pipe { field =>
      field.append(_00001, Side.Front)
    } pipe { field =>
      field.reverse(Seq(0), isReverse = None)
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Back)) =>
          assert(_00001 == card.card)
          assert(field.count() == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverse: 表向きのカードなら裏向きにする (成功)") {
    create() pipe { field =>
      field.append(_00001, Side.Front)
    } pipe { field =>
      field.reverse(Seq(0), isReverse = Some(false))
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Back)) =>
          assert(_00001 == card.card)
          assert(field.count() == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverse: 表向きのカードなら裏向きにする (失敗)") {
    create() pipe { field =>
      field.append(_00001, Side.Back)
    } pipe { field =>
      field.reverse(Seq(0), isReverse = Some(false))
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Back)) =>
          assert(_00001 == card.card)
          assert(field.count() == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverse: 裏向きのカードなら表向きにする (成功)") {
    create() pipe { field =>
      field.append(_00001, Side.Back)
    } pipe { field =>
      field.reverse(Seq(0), isReverse = Some(true))
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Front)) =>
          assert(_00001 == card.card)
          assert(field.count() == _00001.point)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverse: 裏向きのカードなら表向きにする (失敗)") {
    create() pipe { field =>
      field.append(_00001, Side.Front)
    } pipe { field =>
      field.reverse(Seq(0), isReverse = Some(true))
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Front)) =>
          assert(_00001 == card.card)
          assert(field.count() == _00001.point)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverseAllWithFront: 表向きのカードを1枚裏向きにする") {
    create() pipe { field =>
      field.append(_00001, Side.Front)
    } pipe { field =>
      field.reverseAllWithFront()
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Back)) =>
          assert(_00001 == card.card)
          assert(field.count() == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverseAllWithFront: 裏向きのカードを1枚だけならそのまま") {
    create() pipe { field =>
      field.append(_00001, Side.Back)
    } pipe { field =>
      field.reverseAllWithFront()
    } pipe { field =>
      field.fields match {
        case Seq((card, Side.Back)) =>
          assert(_00001 == card.card)
          assert(field.count() == 2)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverseAllWithFront: 混在する場合は表向きのカードのみ裏返す") {
    create() pipe { field =>
      field.append(_00001, Side.Front).append(_00002, Side.Back)
    } pipe { field =>
      field.reverseAllWithFront()
    } pipe { field =>
      field.fields match {
        case Seq((card1, Side.Back), (card2, Side.Back)) =>
          assert(_00001 == card1.card)
          assert(_00002 == card2.card)
          assert(field.count() == 4)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("reverseAll: 全てのカードを裏返す") {
    create() pipe { field =>
      field.append(_00001, Side.Front).append(_00002, Side.Back)
    } pipe { field =>
      field.reverseAll()
    } pipe { field =>
      field.fields match {
        case Seq((card1, Side.Back), (card2, Side.Front)) =>
          assert(_00001 == card1.card)
          assert(_00002 == card2.card)
          assert(field.count() == 2 + _00002.point)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("appendAll: フィールドに裏向きで複数枚カードが追加できる") {
    create() pipe { field =>
      field.appendAll(Seq(_00001, _00002), reverse = true)
    } pipe { field =>
      field.fields match {
        case Seq((card1, Side.Back), (card2, Side.Back)) =>
          assert(_00001 == card1.card)
          assert(_00002 == card2.card)
          assert(field.count() == 4)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("appendAll: フィールドに表向きで複数枚カードが追加できる") {
    create() pipe { field =>
      field.appendAll(Seq(_00001, _00002), reverse = false)
    } pipe { field =>
      field.fields match {
        case Seq((card1, Side.Front), (card2, Side.Front)) =>
          assert(_00001 == card1.card)
          assert(_00002 == card2.card)
          assert(field.count() == _00001.point + _00002.point)
        case _ => fail("追加されたカードが不正な状態")
      }
    }
  }

  test("removeAll: フィールドから先頭のカードを1枚取り出す") {
    create() pipe { field =>
      field.appendAll(Seq(_00001, _00002), reverse = false)
    } pipe { field =>
      field.removeAll(Seq(0))
    } pipe {
      case (field, cards) =>
        assert(cards.map(_.card) == Seq(_00001))
        field.fields match {
          case Seq((card, Side.Front)) =>
            assert(_00002 == card.card)
            assert(field.count() == _00002.point)
          case _ => fail("追加されたカードが不正な状態")
        }
    }
  }

  test("get: UUIDの一致するカードの座標を取得できる") {
    val target = MutableCard(_00001)
    create() pipe { field =>
      field.appendAll(Seq(target, _00002), reverse = false)
    } pipe { field =>
      assert(field.get(target) == 0)
    }
  }

  test("get: UUIDの一致するカードがない場合、-1") {
    create() pipe { field =>
      field.appendAll(Seq(_00001, _00002), reverse = false)
    } pipe { field =>
      assert(field.get(_00001) == -1)
    }
  }
}

package com.github.rapid_bout.game
import com.github.rapid_bout.game.Side.Front

case class Field private (fields: Seq[(MutableCard, Side)]) {

  /**
    * 場上の打点の合計を計算する
    * @return 合計打点
    */
  def count(): Int = fields.foldLeft(0) {
    case (sum, (_, Side.Back)) => sum + 2
    case (sum, (card, Side.Front)) => sum + card.point
  }

  /**
    * 場にカードを置く
    * @param card 置くカード
    * @param side 裏向きで置くか、表向きで置くか
    */
  def append(card: MutableCard, side: Side): Field = copy(fields = fields :+ (card, side))

  /**
    * 場のカードを裏返す
    * @param indexes 裏返したいカードの座標
    * @param isReverse 元々裏のカードまたは表のカードであることが必須か
    */
  def reverse(indexes: Seq[Int], isReverse: Option[Boolean]): Field = copy(
    fields.zipWithIndex.map { case ((card, side), i) =>
      if (indexes.contains(i) && isReverse.fold[Boolean](true)(_ == side.isReverse))
        (card, side.opposite)
      else (card, side)
    }
  )

  /**
    * 場の表向きのカード全てを裏返す
    */
  def reverseAllWithFront(): Field = copy(
    fields = fields.map {
      case (card, side @ Front) => (card, side.opposite)
      case (card, side) => (card, side)
    }
  )

  /**
    * 全てのカードを裏返す
    */
  def reverseAll(): Field = copy(
    fields = fields.map { case (card, side) => (card, side.opposite) }
  )

  /**
    * 場に複数のカードを置く
    * @param cards 追加するカードのリスト
    * @param reverse 裏向きかどうか
    */
  def appendAll(cards: Seq[MutableCard], reverse: Boolean): Field = {
    val side = Side.fromBoolean(reverse)
    copy(fields = fields ++ cards.map((_, side)))
  }

  /**
    * 場から複数のカードを取り除く
    * @param indexes 取り除きたいカードの座標
    * @return 取り出せたカードのリスト
    */
  def removeAll(indexes: Seq[Int]): (Field, List[MutableCard]) = {
    val (fields, cards) = this.fields.zipWithIndex.partitionMap {
      case (card, i) if indexes.contains(i) => Right(card)
      case (card, _) => Left(card)
    }
    (copy(fields = fields), cards.map(_._1).toList)
  }

  /**
    * 特定のカードがある先頭の位置を返す
    * @param target チェックしたいカード
    * @return カードのある座標 (存在しない場合は -1)
    */
  def get(target: MutableCard): Int = fields.indexWhere { case (card, _) => card.uuid == target.uuid }
}

object Field {
  def apply(): Field = new Field(Seq.empty)
}

package battleship.utils

import scalafx.scene.control.Label
import scalafx.scene.effect.{Blend, BlendMode, DropShadow, InnerShadow}
import scalafx.scene.paint.Color

object FancyTitle {
  def applyFancyTitleEffectOn(titleLabel : Label): Unit = {
    // https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/text-effects.htm
    // Text Effects
    val blend = new Blend()
    blend.setMode(BlendMode.Multiply)

    val dropShadow = new DropShadow()
    dropShadow.setColor(Color.rgb(254, 235, 66, 0.3))
    dropShadow.setOffsetX(5)
    dropShadow.setOffsetY(5)
    dropShadow.setRadius(5)

    blend.setBottomInput(dropShadow)

    val ds1 = new DropShadow()
    ds1.setColor(Color.web("#f13a00"))
    ds1.setRadius(20)
    ds1.setSpread(0.2)

    val blend2 = new Blend()
    blend2.setMode(BlendMode.Multiply)

    val is = new InnerShadow()
    is.setColor(Color.web("#feeb42"))
    is.setRadius(9)
    is.setChoke(0.8)
    blend2.setBottomInput(is)

    val is1 = new InnerShadow()
    is1.setColor(Color.web("#f13a00"))
    is1.setRadius(5)
    is1.setChoke(0.4)
    blend2.setTopInput(is1)

    val blend1 = new Blend()
    blend1.setMode(BlendMode.Multiply)
    blend1.setBottomInput(ds1)
    blend1.setTopInput(blend2)

    blend.setTopInput(blend1)

    titleLabel.setEffect(blend)
  }
}

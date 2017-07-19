package io.github.tszypenbejl.simpledrawingapplication

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{Pane, Region}
import scalafx.scene.paint.Color.{Black, White}


private class DrawingCanvas(size: Region) extends Canvas {
  private val brushSize = 6
  private val halfBrushSize = brushSize / 2
  private val g = graphicsContext2D

  private def clearRect(x: Double, y: Double, w: Double, h: Double) = {
    g.fill = White
    g.fillRect(x, y, w, h)
  }

  private def drawBrush(x: Int, y: Int) = {
    g.fill = Black
    g.fillRect(x - halfBrushSize, y - halfBrushSize, brushSize, brushSize)
  }

  private def handleMouseEvent(e: MouseEvent) = drawBrush(e.x.toInt, e.y.toInt)
  onMouseClicked = handleMouseEvent _
  onMouseDragged = handleMouseEvent _

  width.onChange((_, oldVal, newVal) => {
    val oldWidth = oldVal.doubleValue
    val newWidth = newVal.doubleValue
    if (newWidth > oldWidth)
      clearRect(oldWidth, 0, newWidth - oldWidth, height.value)
  })
  height.onChange((_, oldVal, newVal) => {
    val oldHeight = oldVal.doubleValue
    val newHeight = newVal.doubleValue
    if (newHeight > oldHeight)
      clearRect(0, oldHeight, width.value, newHeight - oldHeight)
  })

  width <== size.width
  height <== size.height

  def clear() = clearRect(0, 0, width.value, height.value)
}


object DrawingApp extends JFXApp {
  stage = new PrimaryStage {
    title = "ScalaFX Simple paint application"
    minWidth = 100
    minHeight = 100
    scene = new Scene {
      root = new Pane {
        children = new DrawingCanvas(this)
      }
    }
  }
}

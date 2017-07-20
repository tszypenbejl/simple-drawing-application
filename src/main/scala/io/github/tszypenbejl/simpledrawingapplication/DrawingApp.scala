package io.github.tszypenbejl.simpledrawingapplication

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.ObjectProperty
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.ColorPicker
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.Color


private class DrawingCanvas(size: Region) extends Canvas {
  private val brushSize = 6
  private val halfBrushSize = brushSize / 2
  private val g = graphicsContext2D
  private var prevPoint: (Double, Double) = null

  private def clearRect(x: Double, y: Double, w: Double, h: Double) = {
    g.fill = Color.White
    g.fillRect(x, y, w, h)
  }

  private def drawBrush(x: Double, y: Double) = {
    g.fill = brushColor.value
    g.fillOval(x - halfBrushSize, y - halfBrushSize, brushSize, brushSize)
    prevPoint = (x, y)
  }

  private def drawLine(x: Double, y: Double) = {
    if (prevPoint ne null) {
      g.stroke = brushColor.value
      g.lineWidth = brushSize
      g.strokeLine(prevPoint._1, prevPoint._2, x, y)
    }
    drawBrush(x, y)
  }

  onMousePressed = (e: MouseEvent) => drawBrush(e.x, e.y)
  onMouseDragged = (e: MouseEvent) => drawLine(e.x, e.y)

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

  val brushColor = new ObjectProperty[javafx.scene.paint.Color]


  def clear() = clearRect(0, 0, width.value, height.value)
}


object DrawingApp extends JFXApp {
  stage = new PrimaryStage {
    title = "ScalaFX Simple paint application"
    minWidth = 100
    minHeight = 100
    scene = new Scene {
      private var drawingCanvas: DrawingCanvas = null
      private var colorPicker: ColorPicker = null
      root = new VBox {
        children = Seq(
          new Pane {
            children = { drawingCanvas = new DrawingCanvas(this); drawingCanvas }
            VBox.setVgrow(this, Priority.Always)
          },
          new HBox {
            children = { colorPicker = new ColorPicker(Color.Black); colorPicker }
          }
        )
      }
      drawingCanvas.brushColor <== colorPicker.value
    }
  }
}

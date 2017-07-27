package io.github.tszypenbejl.simpledrawingapplication

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.ObjectProperty
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{Button, ColorPicker}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.Color


private class DrawingCanvas(size: Region) extends Canvas {
  private val brushSize = 6
  private val halfBrushSize = brushSize / 2
  private val g = graphicsContext2D
  private var prevPoint: (Double, Double) = null

  private def clearRect(x: Double, y: Double, w: Double, h: Double) = {
    g.fill = backgroundColor.value
    g.fillRect(x, y, w, h)
  }

  private def drawBrush(x: Double, y: Double, useBackgroundColor: Boolean) = {
    g.fill = if (useBackgroundColor) backgroundColor.value else brushColor.value
    g.fillOval(x - halfBrushSize, y - halfBrushSize, brushSize, brushSize)
    prevPoint = (x, y)
  }

  private def drawLine(x: Double, y: Double, useBackgroundColor: Boolean) = {
    if (prevPoint ne null) {
      g.stroke = if (useBackgroundColor) backgroundColor.value else brushColor.value
      g.lineWidth = brushSize
      g.strokeLine(prevPoint._1, prevPoint._2, x, y)
    }
    prevPoint = (x, y)
  }

  onMousePressed = (e: MouseEvent) => drawBrush(e.x, e.y, e.secondaryButtonDown)
  onMouseDragged = (e: MouseEvent) => drawLine(e.x, e.y, e.secondaryButtonDown)

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
  val backgroundColor = new ObjectProperty[javafx.scene.paint.Color]


  def clear() = clearRect(0, 0, width.value, height.value)
}


object DrawingApp extends JFXApp {
  stage = new PrimaryStage {
    title = "ScalaFX Simple paint application"
    minWidth = 100
    minHeight = 100
    scene = new Scene {
      private var drawingCanvas: DrawingCanvas = null
      private var colorPicker1: ColorPicker = null
      private var colorPicker2: ColorPicker = null
      private var clearButton: Button = null;
      root = new VBox {
        children = Seq(
          new Pane {
            children = { drawingCanvas = new DrawingCanvas(this); drawingCanvas }
            VBox.setVgrow(this, Priority.Always)
          },
          new HBox {
            children = Seq(
              { colorPicker1 = new ColorPicker(Color.Black); colorPicker1 },
              { colorPicker2 = new ColorPicker(Color.White); colorPicker2 },
              { clearButton = new Button("Clear"); clearButton }
            )
            clearButton.maxWidth = Double.MaxValue
            HBox.setHgrow(clearButton, Priority.Always)
          }
        )
      }
      drawingCanvas.brushColor <== colorPicker1.value
      drawingCanvas.backgroundColor <== colorPicker2.value
      clearButton.onAction = handle { drawingCanvas.clear() }
    }
  }
}

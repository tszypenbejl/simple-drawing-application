package io.github.tszypenbejl.simpledrawingapplication

import scala.collection.mutable
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
import scalafx.scene.shape.StrokeLineCap


private object MyGeom {
  def lineEquationParams(p1: (Double, Double), p2: (Double, Double)) = {
    val dx = p2._1 - p1._1
    val dy = p2._2 - p1._2
    assert(dx != 0 || dy != 0)
    if (dy == 0)
      (0.0, 1.0, p1._2)
    else
      (1.0, -dx / dy, p1._1 - p1._2 * dx / dy)
  }

  def intersection(line1p1: (Double, Double), line1p2: (Double, Double),
                   line2p1: (Double, Double), line2p2: (Double, Double)) = {
    val (a1, b1, c1) = lineEquationParams(line1p1, line1p2)
    val (a2, b2, c2) = lineEquationParams(line2p1, line2p2)
    val w = a1 * b2 - b1 * a2
    val wx = c1 * b2 - b1 * c2
    val wy = a1 * c2 - c1 * a2
    if (w != 0)
      (wx / w, wy / w)
    else
      (Double.NaN, Double.NaN)
  }

  def nyDistance(p1: (Double, Double), p2: (Double, Double)) = math.abs(p2._1 - p1._1) + math.abs(p2._2 - p1._2)

  def sameDirection(p1: (Double, Double), p2: (Double, Double), p3: (Double, Double)) = {
    val dx1 = p2._1 - p1._1
    val dx2 = p3._1 - p2._1
    val dy1 = p2._2 - p1._2
    val dy2 = p3._2 - p2._2
    math.signum(dx1) == math.signum(dx2) && math.signum(dy1) == math.signum(dy2)
  }
}


private class DrawingCanvas(size: Region) extends Canvas {
  private val brushSize = 6
  private val halfBrushSize = brushSize / 2
  private val g = graphicsContext2D
  private var prevPoints = new mutable.ArrayBuffer[(Double, Double)]

  private def clearRect(x: Double, y: Double, w: Double, h: Double) = {
    g.fill = backgroundColor.value
    g.fillRect(x, y, w, h)
  }

  private def drawBrush(x: Double, y: Double, useBackgroundColor: Boolean) = {
    g.fill = if (useBackgroundColor) backgroundColor.value else brushColor.value
    g.fillOval(x - halfBrushSize, y - halfBrushSize, brushSize, brushSize)
    prevPoints.clear()
    prevPoints.append((x, y))
  }

  private def drawLine(x: Double, y: Double, useBackgroundColor: Boolean) = {
    g.stroke = if (useBackgroundColor) backgroundColor.value else brushColor.value
    g.lineCap = StrokeLineCap.Round
    g.lineWidth = brushSize

    if (prevPoints.size == 2 && MyGeom.nyDistance(prevPoints.last, (x, y)) <= brushSize)
      prevPoints.remove(0, 1)

    if (prevPoints.size == 1) {
      g.strokeLine(prevPoints.head._1, prevPoints.head._2, x, y)
    } else if (prevPoints.size == 3) {
      val controlPoint = MyGeom.intersection(prevPoints(0), prevPoints(1), prevPoints(2), (x, y))
      g.beginPath()
      (g.moveTo _).tupled(prevPoints(1))
      if (controlPoint._1.isNaN || controlPoint._2.isNaN || controlPoint._1.isInfinite || controlPoint._2.isInfinite
          || MyGeom.nyDistance(controlPoint, (x, y)) > (width.value + height.value)
          || !(MyGeom.sameDirection(prevPoints(0), prevPoints(1), controlPoint)
                && MyGeom.sameDirection(controlPoint, prevPoints(2), (x, y)))) {
          (g.lineTo _).tupled(prevPoints(2))
      } else {
        g.quadraticCurveTo(controlPoint._1, controlPoint._2, prevPoints(2)._1, prevPoints(2)._2)
      }
      g.lineTo(x, y)
      g.strokePath()
      prevPoints.remove(0, 2)
    }
    prevPoints.append((x, y))
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
    width = 500
    height = 400
    scene = new Scene {
      private var drawingCanvas: DrawingCanvas = null
      private var colorPicker1: ColorPicker = null
      private var colorPicker2: ColorPicker = null
      private var swapColorsButton : Button = null
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
              { swapColorsButton = new Button("<->"); swapColorsButton },
              { clearButton = new Button("Clear"); clearButton }
            )
            clearButton.maxWidth = Double.MaxValue
            HBox.setHgrow(clearButton, Priority.Always)
          }
        )
      }
      drawingCanvas.brushColor <== colorPicker1.value
      drawingCanvas.backgroundColor <== colorPicker2.value
      swapColorsButton.onAction = handle {
        val tmp = colorPicker1.value.value
        colorPicker1.value = colorPicker2.value.value
        colorPicker2.value = tmp
      }
      clearButton.onAction = handle { drawingCanvas.clear() }
    }
  }
}

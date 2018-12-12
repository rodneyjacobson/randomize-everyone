package examples

import java.lang.Math._
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import dom.html

@JSExportTopLevel("Waves")
object Waves extends js.JSApp {

  @JSExport
  def main(): Unit = {
    println("Application starting")
    val canvas: html.Canvas = dom.document.getElementById("canvas").asInstanceOf[dom.html.Canvas]
    println(canvas)

    def clear() = {
      canvas.width = 1000
      canvas.height = 500
    }
    clear()

    val brush =
      canvas.getContext("2d")
        .asInstanceOf[dom.CanvasRenderingContext2D]

    def h = canvas.height
    def w = canvas.width

    var x = 0.0
    type Graph = (String, Double => Double)
    val graphs = Seq[Graph](
      ("red", sin),
      ("green", x => abs(x % 4 - 2) - 1),
      ("blue", x => sin(x) + abs(x % 4 - 2) - 1)
    ).zipWithIndex
    dom.window.setInterval(() => {
      x = x + 1
      val xx = x % w
      if (xx == 0) {
        clear()
      }
      for (((color, f), i) <- graphs) {
        val offset = h / 3 * (i + 0.5)
        val y = f(x / w * 75) * h / 30
        brush.fillStyle = color
        brush.fillRect(xx, y + offset, 3, 3)
      }
    }, 5)
  }
}

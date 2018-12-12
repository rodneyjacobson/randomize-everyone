package examples
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.dom
import org.scalajs.dom.html
import scala.util.Random
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalatags.JsDom.all._
import upickle.default._
import upickle.Js
import autowire._
import scala.scalajs.js
import org.scalajs.jquery._

object Client extends autowire.Client[Js.Value, Reader, Writer] {
  val $ = jQuery

  override def doCall(req: Request): Future[Js.Value] = {
    val csrf = jQuery("[name=csrfToken]")
    val cs: String = $(csrf).value.toString
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upickle.json.write(Js.Obj(req.args.toSeq: _*)),
      headers = Map("Csrf-Token" -> cs)
    ).map(_.responseText).map(res => {
        val rez = upickle.json.read(res)
        rez
      })
  }

  def read[Result: Reader](p: Js.Value) = readJs[Result](p)
  def write[Result: Writer](r: Result) = writeJs(r)
}

@JSExportTopLevel("AutowireDemo")
object AutowireDemo extends js.JSApp {
  @JSExport
  def main(): Unit = {
    val inputBox = input.render
    val outputBox = div.render

    def updateOutput() = {
      outputBox.innerHTML = ""
      try {
        Client[Api].list(inputBox.value).call().map(files => {
          for (file <- files) {
            outputBox.appendChild(
              ul(li(file)).render
            )
          }
        })
      } catch {
        case e: Throwable => {
          println("exception")
          println(s"$e")
        }
      }
    }

    inputBox.onkeyup = { (e: dom.Event) => updateOutput() }

    val target: html.Div = dom.document.getElementById("playground").asInstanceOf[dom.html.Div]
    target.appendChild(
      div(
      h1("File Browser"),
      p("Enter a file path to scan"),
      inputBox,
      outputBox
    ).render
    )
  }
}

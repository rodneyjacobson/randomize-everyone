package examples

import scala.scalajs.js
import scala.scalajs.js.Date

import org.scalajs.dom
import dom.html
import org.scalajs.dom.html.{ Div, Button, Input }
import org.scalajs.dom.raw.MouseEvent
import scala.util.Try
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import scalatags.JsDom.all._
import upickle.default._
import rx._
import dom.ext._
import scala.scalajs
  .concurrent
  .JSExecutionContext
  .Implicits
  .runNow

@JSExportTopLevel("RxDemo")
object RxDemo extends Reactive with js.JSApp {

  var thingsToDo = Var[List[Task]](List.empty)
  val thingsToDoList = div().render

  val addDesc = input("New Task").render
  val addTime = input("0").render
  val addButton = button("Add a new task", `type` := "button", `class` := "btn btn-primary", marginTop := 10, marginBottom := 10).render
  val timeSummary = div().render

  @JSExport
  def main(): Unit = { //(implicit ctx: Ctx.Owner, data: Ctx.Data): Unit = {
    implicit val ctx = Ctx.Owner.Unsafe
    val target: html.Div = dom.document.getElementById("playground").asInstanceOf[dom.html.Div]
    println(s"main")

    fetchData().onSuccess {
      case s =>
        println(s"Got ${s.responseText}")
        thingsToDo() = read[List[Task]](s.responseText)
    }

    target.appendChild(
      rebuildUI(target, addDesc, addTime, addButton)
    )

    /// create chain of reactive variables
    val mouseVar = everyClick(addButton)
    val addTodo = mouseVar.trigger {
      val desc = addDesc.value
      val time = Try { addTime.value.toInt }.toOption.getOrElse(0)
      println(s"Adding $desc/$time")
      addNewTodo(desc, time)
    }

    val timeVar = everySecond()
    val todoTimeCount = Rx {
      thingsToDo().foldLeft(0)((x, y) => x + y.time)
    }
    val todoCount = Rx {
      thingsToDo().length
    }
    val updateTimeSummary = Rx {
      refreshTimeSummary(timeVar(), todoTimeCount())
    }
    val updateTimeSummaryStyle = Rx {
      val count = todoCount()
      if (count >= 5) {
        timeSummary.style.backgroundColor = Color.Red
      } else {
        timeSummary.style.backgroundColor = Color.White
      }
    }
    val updateTodoList = Rx {
      refreshTodoList(target, thingsToDo())
    }
  }

  def addNewTodo(desc: String, time: Int): Unit = {
    thingsToDo() = Task(desc, time) :: thingsToDo.now
  }

  def refreshTodoList(target: Div, todos: List[Task]) = {
    thingsToDoList.innerHTML = ""
    thingsToDoList.appendChild(
      ul(
      for (it <- todos) yield li(
        div(
          s"${it.desc} takes ${it.time} minutes",
          createDeleteButton(target, it)
        ),
        `class` := "list-group-item"
      ),
      `class` := "list-group"
    ).render
    )
  }

  def fetchData() = Ajax.get("https://sizzling-torch-788.firebaseio.com/dummyTask.json")

  def rebuildUI(target: html.Div, addDesc: Input, addTime: Input, addButton: Button): Div =
    div(
      h1("ScalaRx + Scala.js organizer"),
      thingsToDoList,
      addForm,
      timeSummary,
      `class` := "col-sm-4"
    ).render

  def createDeleteButton(target: Div, it: Task) = {
    val b = button("X", `class` := "btn btn-sm btn-danger", float.right, marginTop := -5).render
    b.onclick = (_: MouseEvent) => {
      thingsToDo() = thingsToDo.now.filterNot(_ == it)
    }
    b
  }

  val addForm = Array(
    div("Task: ", addDesc, br, "Mins: ", addTime),
    div(addButton)
  )

  def format(d: Date) = f"${d.getHours()}:${d.getMinutes()}%02d:${d.getSeconds()}%02d"
  def refreshTimeSummary(timestamp: Double, timeNeeded: Int) = {
    val now: Date = new Date(timestamp)
    val endDate = new Date(timestamp + timeNeeded * 60000)

    timeSummary.innerHTML = s"${format(now)} + ${timeNeeded} minutes on tasks = ${format(endDate)}"
  }
}

case class Task(desc: String, time: Int)

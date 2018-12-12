package examples
import upickle.default._
import upickle.Js
import scala.concurrent.ExecutionContext.Implicits.global

object AutowireServer extends autowire.Server[Js.Value, Reader, Writer] {
  def read[Result: Reader](p: Js.Value) = upickle.default.readJs[Result](p)
  def write[Result: Writer](r: Result) = upickle.default.writeJs(r)

  val routes = AutowireServer.route[Api](Server)
}

object Server extends Api {
  def list(path: String) = {
    val chunks = path.split("/", -1)
    val prefix = "./" + chunks.dropRight(1).mkString("/")
    val files = Option(new java.io.File(prefix).list()).toSeq.flatten
    files.filter(_.startsWith(chunks.last))
  }
}

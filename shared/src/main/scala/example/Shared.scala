package examples

trait Api {
  def list(path: String): Seq[String]
}

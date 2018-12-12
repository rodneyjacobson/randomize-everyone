package models.daos

import javax.inject.Inject
import scala.util.Random.shuffle
import models.{ Assignment, Study }
import models.daos.AssignmentDAOImpl._
import models.tables._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Give access to the assignment object.
 */
class AssignmentDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends AssignmentDAO {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.driver.api._

  /**
   * Finds a assignments by study ID.
   *
   * @param studyID The ID of the study to find assignments of.
   * @return The found assignments
   */
  def find(studyId: Int): Future[Seq[Assignment]] = {
    val assignmentQuery = assignments.filter(_.studyId === studyId)
    db.run(assignmentQuery.result)
  }

  /**
   * Finds all assignments
   *
   * @return The found assignments.
   */
  def find(): Future[Seq[Assignment]] = db.run(assignments.result)

  def newBlock: Future[Seq[Assignment]] = {
    ???
  }

  def find(study: Study): Future[Seq[Assignment]] = {
    val assignmentQuery = assignments.filter(_.studyId === study.studyId)
    db.run(assignmentQuery.result)
  }

  def addBlock(study: Study): Future[Option[Int]] = {
    val deck = (1 to study.groups).flatMap(n => List.fill(study.blockSize)(n))
    val block: IndexedSeq[Int] = shuffle(deck)
    val newA = block.zipWithIndex.map { case (gp, n) => Assignment(study.studyId, n, None, gp) }
    val insert = (assignments ++= newA)
    db.run(insert)
  }

  /**
   * Saves a assignment.
   *
   * @param assignment The assignment to save.
   * @return The saved assignment.
   */
  def save(assignment: Assignment): Future[Int] = {
    db.run(assignments += assignment)
  }
}

object AssignmentDAOImpl {
  private val assignments = TableQuery[AssignmentTable]
}

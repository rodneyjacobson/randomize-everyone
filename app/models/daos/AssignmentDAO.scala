package models.daos

import models.{ Assignment, Study }
import models.tables.{ DbLoginInfo, LoginInfoTable }
import slick.lifted.Query

import scala.concurrent.Future

/**
 * Give access to the assignment object.
 */
trait AssignmentDAO {

  def find(studyId: Int): Future[Seq[Assignment]]

  def find(): Future[Seq[Assignment]]

  def addBlock(study: Study): Future[Option[Int]]

  def save(assignment: Assignment): Future[Int]
}

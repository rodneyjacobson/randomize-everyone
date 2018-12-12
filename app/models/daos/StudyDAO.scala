package models.daos

import models.Study
import models.tables.{ DbLoginInfo, LoginInfoTable }
import slick.lifted.Query

import scala.concurrent.Future

/**
 * Give access to the study object.
 */
trait StudyDAO {

  def find(studyId: Int): Future[Option[Study]]

  def findUrl(url: String): Future[Option[Study]]

  def find(): Future[Seq[Study]]

  def save(study: Study): Future[Int]
}

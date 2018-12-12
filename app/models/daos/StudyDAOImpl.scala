package models.daos

import javax.inject.Inject

import models.Study
import models.daos.StudyDAOImpl._
import models.tables._
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Give access to the study object.
 */
class StudyDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends StudyDAO {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.driver.api._

  /**
   * Finds a study by its study ID.
   *
   * @param studyID The ID of the study to find.
   * @return The found study or None if no study for the given ID could be found.
   */
  def find(studyId: Int): Future[Option[Study]] = {
    val studyQuery = studys.filter(_.studyId === studyId)
    db.run(studyQuery.result.headOption)
  }

  def findUrl(url: String): Future[Option[Study]] = {
    val studyQuery = studys.filter(_.url === url)
    db.run(studyQuery.result.headOption)
  }

  /**
   * Finds all studys
   *
   * @return The found studys.
   */
  def find(): Future[Seq[Study]] = db.run(studys.result)

  /**
   * Saves a study.
   *
   * @param study The study to save.
   * @return The saved study.
   */
  def save(study: Study): Future[Int] = db.run(studys += study)
}

object StudyDAOImpl {
  private val studys = TableQuery[StudyTable]
}

package models.services

import javax.inject.Inject

import models.Study
import models.daos.StudyDAO

import scala.concurrent.Future

/**
 * Handles actions to studys.
 *
 * @param studyDAO The study DAO implementation.
 */
class StudyServiceImpl @Inject() (studyDAO: StudyDAO) extends StudyService {

  /**
   * Retrieves a study that matches the specified ID.
   *
   * @param id The ID to retrieve a study.
   * @return The retrieved study or None if no study could be retrieved for the given ID.
   */
  def retrieve(id: Int) = studyDAO.find(id)

  /**
   * Retrieves all studys.
   *
   * @return The retrieved studys.
   */
  def find(): Future[Seq[Study]] = studyDAO.find()

  def findUrl(url: String): Future[Option[Study]] = studyDAO.findUrl(url)

  /**
   * Saves a study.
   *
   * @param study The study to save.
   * @return The saved study.
   */
  def save(study: Study) = studyDAO.save(study)

}

package models.services

import javax.inject.Inject
import models.{ Assignment, Study }
import models.daos.AssignmentDAO

import scala.concurrent.Future

/**
 * Handles actions to assignments.
 *
 * @param assignmentDAO The assignment DAO implementation.
 */
class AssignmentServiceImpl @Inject() (assignmentDAO: AssignmentDAO) extends AssignmentService {

  /**
   * Retrieves a assignment that matches the specified ID.
   *
   * @param id The ID to retrieve a assignment.
   * @return The retrieved assignment or None if no assignment could be retrieved for the given ID.
   */
  def find(id: Int) = assignmentDAO.find(id)

  def addBlock(study: Study) = assignmentDAO.addBlock(study)

  def find(study: Study) = assignmentDAO.find(study.studyId)

  /**
   * Retrieves all assignments.
   *
   * @return The retrieved assignments.
   */
  def find(): Future[Seq[Assignment]] = assignmentDAO.find()

  /**
   * Saves a assignment.
   *
   * @param assignment The assignment to save.
   * @return The saved assignment.
   */
  def save(assignment: Assignment) = assignmentDAO.save(assignment)

}

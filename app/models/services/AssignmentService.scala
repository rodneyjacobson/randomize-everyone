package models.services

import models.{ Assignment, Study }

import scala.concurrent.Future

trait AssignmentService {

  def find(id: Int): Future[Seq[Assignment]]

  def find(study: Study): Future[Seq[Assignment]]

  def addBlock(study: Study): Future[Option[Int]]

  def find(): Future[Seq[Assignment]]

  def save(assignment: Assignment): Future[Int]

}

package models.services

import models.Study

import scala.concurrent.Future

trait StudyService {

  def retrieve(id: Int): Future[Option[Study]]

  def find(): Future[Seq[Study]]

  def findUrl(url: String): Future[Option[Study]]

  def save(study: Study): Future[Int]

}

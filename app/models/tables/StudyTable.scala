package models.tables
import models.Study

import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape

class StudyTable(tag: Tag) extends Table[Study](tag, "study") {

  def studyId = column[Int]("study_id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")
  def url = column[String]("url")
  def groups = column[Int]("groups")
  def blockSize = column[Int]("block_size")

  override def * = (studyId, name, url, groups, blockSize) <> (Study.tupled, Study.unapply)
}

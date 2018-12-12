package models.tables
import java.sql.Timestamp
import models.Assignment

import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape

class AssignmentTable(tag: Tag) extends Table[Assignment](tag, "assignment") {
  def studyId = column[Int]("study_id")
  def order = column[Int]("ord")
  def dateAssigned = column[Option[Timestamp]]("date_assigned")
  def group = column[Int]("group_num")
  override def * = (studyId, order, dateAssigned, group) <> (Assignment.tupled, Assignment.unapply)
}

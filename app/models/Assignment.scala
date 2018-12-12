package models

import java.sql.Timestamp

case class Assignment(
  studyId: Int,
  order: Int,
  dateAssigned: Option[Timestamp],
  group: Int
)

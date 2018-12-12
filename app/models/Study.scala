package models

case class Study(
  studyId: Int,
  name: String,
  url: String,
  groups: Int,
  blockSize: Int // how many of each group to add at once when table is exhausted
)

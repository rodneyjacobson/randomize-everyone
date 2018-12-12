package forms.pages

import play.api.data.Form
import play.api.data.Forms._
import models.Study

/**
 * The form which handles new studies
 */
object StudyForm {

  val form = Form(
    mapping(
      "studyId" -> ignored(0),
      "name" -> nonEmptyText,
      "url" -> nonEmptyText,
      "groups" -> number,
      "blockSize" -> number
    )(Study.apply)(Study.unapply)
  )
}

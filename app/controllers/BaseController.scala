package controllers.pages

import javax.inject._

import play.api.Logger
import play.api.mvc._

import upickle.default._
import play.api.i18n.{ I18nSupport, MessagesApi }
import models._
import scala.concurrent.Future
import scala.language.implicitConversions

trait BaseController extends Controller with I18nSupport {
  implicit def result2Future(res: Result): Future[Result] = Future.successful(res)

}

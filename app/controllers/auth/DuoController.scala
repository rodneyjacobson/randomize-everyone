package controllers.auth

import javax.inject.Inject
import com.duosecurity.duoweb.DuoWeb.{ signRequest, verifyResponse }

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import controllers.{ WebJarAssets, auth, pages }
import forms.auth.SignInForm
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{ Action, AnyContent, Controller }
import utils.auth.DefaultEnv
import controllers.pages
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import play.api.data._
import play.api.data.Forms._

class DuoController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  lazy val c = configuration.underlying
  lazy val ikey = c.as[String]("silhouette.duo.ikey")
  lazy val skey = c.as[String]("silhouette.duo.skey")
  lazy val akey = c.as[String]("silhouette.duo.akey")

  def duo = silhouette.UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => user.email match {
        case Some(email) => {
          val sr = signRequest(ikey, skey, akey, email)
          Ok(views.html.auth.duo(sr))
        }
        case None => Redirect(auth.routes.SignInController.view)
      }
      case None => Redirect(auth.routes.SignInController.view)
    }
  }

  case class DuoData(val sigResponse: String)

  val duoForm = Form(
    mapping(
      "sig_response" -> text
    )(DuoData.apply)(DuoData.unapply)
  )

  def handleDuo = silhouette.UserAwareAction { implicit request =>
    duoForm.bindFromRequest.fold(
      formWithErrors => {
        Redirect(pages.routes.ApplicationController.index)
      },
      duoData => {
        val authenticatedUser = verifyResponse(ikey, skey, akey, duoData.sigResponse)
        (request.session.get("target") match {
          case Some(uri) => Redirect(uri)
          case None => Redirect(pages.routes.ApplicationController.index())
        }).withSession("duo" -> authenticatedUser)
      }
    )
  }

}

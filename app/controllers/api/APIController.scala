package controllers.pages

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import controllers.{ WebJarAssets, pages }
import models.services.UserService
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, AnyContent, Controller }
import play.api.data._
import play.api.data.Forms._
import utils.auth.DefaultEnv
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import autowire._
import upickle.default._
import upickle.Js
import examples.{ Api, AutowireServer, Server }

/**
 * The API controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param socialProviderRegistry The social provider registry.
 * @param webJarAssets The webjar assets implementation.
 */
class APIController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  socialProviderRegistry: SocialProviderRegistry,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  case class ApiRequest(pickledRequest: String, pickledMetadata: String)

  val requestForm = Form(
    mapping(
      "pickledRequest" -> nonEmptyText,
      "pickledMetadata" -> nonEmptyText
    )((pickledRequest, pickledMetadata) => ApiRequest(pickledRequest, pickledMetadata))(apiRequest => Some((apiRequest.pickledRequest, apiRequest.pickledMetadata)))
  )

  def write[Result: Writer](r: Result) = upickle.default.write(r)
  def read[Result: Reader](p: String) = upickle.default.read[Result](p)

  def apiRequest: Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    val body = request.body.toString
    val bodys = body.dropWhile(_ != '(').init.drop(1)
    val pmap = upickle.json.read(bodys).asInstanceOf[Js.Obj].value.toMap
    val aw = autowire.Core.Request(Seq("examples", "Api", "list"), pmap)
    (AutowireServer.route[Api](Server)(aw)).map(as => {
      val res = upickle.json.write(as)
      Ok(res)
    })
  }
}

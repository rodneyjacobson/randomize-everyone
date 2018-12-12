package controllers.auth

import javax.inject.Inject
import java.util.UUID

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import controllers.{ WebJarAssets, auth, pages }
import forms.auth.SignInForm
import models.services.{ AuthTokenService, UserService }
import models.User
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{ Action, AnyContent, Controller }
import utils.auth.DefaultEnv
import org.apache.directory.ldap.client.api._
import org.apache.directory.api.ldap.model.cursor._
import org.apache.directory.api.ldap.model.message._
import org.apache.directory.api.ldap.model.entry._

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * The `Sign In` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param credentialsProvider    The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration          The Play configuration.
 * @param clock                  The clock instance.
 * @param webJarAssets           The webjar assets implementation.
 */
class SignInController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  credentialsProvider: CredentialsProvider,
  passwordHasherRegistry: PasswordHasherRegistry,
  socialProviderRegistry: SocialProviderRegistry,
  avatarService: AvatarService,
  configuration: Configuration,
  clock: Clock,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  /**
   * Views the `Sign In` page.
   *
   * @return The result to display.
   */
  def view: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.auth.signIn(SignInForm.form, socialProviderRegistry)))
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request =>

    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.signIn(form, socialProviderRegistry))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val result = request.session.get("target") match {
            case Some(uri) => Redirect(uri)
            case None => Redirect(pages.routes.ApplicationController.index())
          }
          userService.retrieve(loginInfo).flatMap {
            case Some(user) if !user.activated =>
              Future.successful(Ok(views.html.auth.activateAccount(data.email)))
            case Some(user) =>
              val c = configuration.underlying
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator if data.rememberMe =>
                  authenticator.copy(
                    expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                    idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                    cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
                  )
                case authenticator => authenticator
              }.flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                  silhouette.env.authenticatorService.embed(v, result)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case e: ProviderException =>
            Redirect(auth.routes.SignInController.view()).flashing("error" -> Messages("invalid.credentials" + " 1"))
        }
      }
    )
  }

  private def getLDAP(netid: String): (Option[String], Option[String], Option[String]) = {
    val connection: LdapConnection = new LdapNetworkConnection("ldap.dartmouth.edu", 389)
    connection.bind()
    //val cursor: EntryCursor = connection.search("ou=system", "(objectclass=*)", SearchScope.ONELEVEL, "*")
    val cursor: EntryCursor = connection.search("dc=dartmouth,dc=edu", s"(uid=$netid)", SearchScope.SUBTREE, "mail", "givenName", "sn")
    if (cursor.next()) {
      val entry: Entry = cursor.get()
      val mail: String = s"${entry.get("mail")}"
      val givenName: String = s"${entry.get("givenName")}"
      val sn: String = s"${entry.get("sn")}"
      (Some(mail.split(" ", 2)(1)), Some(givenName.split(" ", 2)(1)), Some(sn.split(" ", 2)(1)))
    } else {
      (None, None, None)
    }
  }

  def dartmouth: Action[AnyContent] = Action.async { implicit request =>
    val netidOption = request.headers.get("cas-attr-netid")
    if (netidOption == None) {
      Future.successful(Redirect(auth.routes.SignInController.view()).flashing("error" -> Messages("invalid.credentials" + " 2")))
    } else {
      val netid = netidOption.get.toLowerCase()
      val (emailO, givenName, sn) = getLDAP(netid)

      val email = (emailO.getOrElse(netid + "@dartmouth.edu")).toLowerCase
      // val givenName: String = givenNameO.getOrElse(netid)
      // val sn: String = snO.getOrElse(netid)

      val loginInfo = new LoginInfo(CredentialsProvider.ID, email)
      val result = request.session.get("target") match {
        case Some(uri) => Redirect(uri)
        case None => Redirect(pages.routes.ApplicationController.index())
      }

      val u = Await.result(userService.retrieve(loginInfo), 15 seconds)

      if (u == None) {
        val createTask: Future[play.api.mvc.Result] = {
          val authInfo = passwordHasherRegistry.current.hash(UUID.randomUUID().toString)
          val user = User(
            userID = UUID.randomUUID(),
            loginInfo = loginInfo,
            firstName = givenName,
            lastName = sn,
            netID = Some(netid),
            email = Some(email),
            avatarURL = None,
            activated = true
          // Could start activated as false to allow only netid sign in.
          // Would need to request email verification before change password etc
          )
          for {
            avatar <- avatarService.retrieveURL(email)
            user <- userService.save(user.copy(avatarURL = avatar))
            authInfo <- authInfoRepository.add(loginInfo, authInfo)
            authToken <- authTokenService.create(user.userID)
          } yield {
            val url = auth.routes.ActivateAccountController.activate(authToken.id).absoluteURL()
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            result
          }
        }
        Await.result(createTask, 15 seconds)
      }
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator => authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
              silhouette.env.authenticatorService.embed(v, result)
            }
          }
        case None => {
          Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
    }
  }

}

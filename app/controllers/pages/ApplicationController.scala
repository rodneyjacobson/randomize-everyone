package controllers.pages

import models.{ Card, Service, ServiceRequest, Services, Source }
import play.api.libs.json._
import play.api.libs.functional.syntax._

import javax.inject.Inject
import scala.util.Random.shuffle

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import controllers.{ WebJarAssets, pages }
import models.Study
import models.services.{ AssignmentService, StudyService, UserService }
import play.api.data.Form
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, AnyContent, Controller }
import utils.auth.{ DefaultEnv, WithDuo }
import scala.concurrent.ExecutionContext.Implicits.global
import forms.pages.StudyForm

import scala.concurrent.Future
import utils.auth.{ DefaultEnv, WithProvider }

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param socialProviderRegistry The social provider registry.
 * @param webJarAssets The webjar assets implementation.
 */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  studyService: StudyService,
  assignmentService: AssignmentService,
  socialProviderRegistry: SocialProviderRegistry,
  implicit val webJarAssets: WebJarAssets)
  extends BaseController {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index: Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  def study(studyUrl: String) = silhouette.UserAwareAction.async { implicit request =>
    studyService.findUrl(studyUrl).map(study => {
      study match {
        case None => ???
        case Some(study) => {
          Ok(views.html.home(request.identity))
        }
      }
    })
  }

  def cdsService(study: String, service: String) = silhouette.UserAwareAction.async(parse.json[ServiceRequest]) { implicit request =>
    val sr = request.body
    if (sr.hook != "patient-randomize") {
      BadRequest(s"Unhandled hook: ${sr.hook}\n")
    } else service match {
      case "randomize-patient" => {
        getRandom(study).map { rgroup =>
          val card = Card(s"Assigned to group ${rgroup}", None, "info", Source("Randomize Everyone"))
          Ok(Json.toJson(card))
        }
      }
      case _ => BadRequest(s"Unhandled service: $service\n")
    }
  }

  def cdsTest(study: String) = silhouette.UserAwareAction.async { implicit request =>
    Ok(views.html.cdsForm(request.identity))
  }

  def discovery(study: String) = silhouette.UserAwareAction.async { implicit request =>
    val service = Service("patient-randomize", "Patient Randomizer", "A service to randomize the patient into a study group", "patient-randomizer", None) //Some(Seq(Fetch("Patient/{{context.patientId}}", None))))
    val services = Services(Seq(service))
    Ok(Json.toJson(services))
  }

  def getRandom(studyUrl: String): Future[String] = {
    for {
      study <- studyService.findUrl(studyUrl)
      assignments <- assignmentService.find(study.get.studyId)
    } yield {
      if (assignments.size < 1) {
      }
      val s: Study = study.get
      s.url + scala.util.Random.nextInt(s.groups)
    }
  }

  def randomize(studyUrl: String) = silhouette.UserAwareAction.async { implicit request =>
    for {
      study <- studyService.findUrl(studyUrl)
      assignments <- assignmentService.find(study.get.studyId)
    } yield {
      if (assignments.size < 1) {
      }
      val s: Study = study.get
      val group = s.url + scala.util.Random.nextInt(s.groups)
      Ok(s"$group")
    }
  }

  case class GroupAssignment(name: String, url: String, group: String)
  implicit val groupWrites2 = Json.writes[GroupAssignment]

  def randomizeJson(studyUrl: String) = silhouette.UserAwareAction.async { implicit request =>
    for {
      study <- studyService.findUrl(studyUrl)
      assignments <- assignmentService.find(study.get.studyId)
    } yield {
      if (assignments.size < 1) {
      }
      val s: Study = study.get
      val group = s.url + scala.util.Random.nextInt(s.groups)
      val ga = GroupAssignment(s.name, s.url, group)
      val j = Json.toJson(ga)
      Ok(j)
    }
  }

  def join(studyUrl: String) = silhouette.UserAwareAction.async { implicit request =>
    for {
      study <- studyService.findUrl(studyUrl)
      assignments <- assignmentService.find(study.get.studyId)
    } yield {
      val s: Study = study.get
      Ok(views.html.joinStudy(s))
    }
  }

  def randomize2(studyUrl: String) = silhouette.UserAwareAction.async { implicit request =>
    for {
      study <- studyService.findUrl(studyUrl)
      assignments <- assignmentService.find(study.get.studyId)
    } yield {
      if (assignments.size < 1) {
      }
      val s: Study = study.get
      val group = s.url + scala.util.Random.nextInt(s.groups)
      Ok(views.html.getGroup(group))
    }
  }

  def addBlock(studyUrl: String) = silhouette.UserAwareAction.async { implicit request =>
    for {
      study <- studyService.findUrl(studyUrl)
      assignments <- assignmentService.addBlock(study.get)
    } yield {
      Ok(s"Added new block")
    }
  }

  def handleNewStudy = silhouette.UserAwareAction.async { implicit request =>
    println("hns")
    StudyForm.form.bindFromRequest.fold(
      formWithErrors => {
        println("errors")
        Future.successful(BadRequest(views.html.newStudy(request.identity, formWithErrors)))
      },
      study => {
        println("no errors")
        studyService.save(study).map(_ => {
          println(s"ss ${study.url}")
        })
        Redirect(pages.routes.ApplicationController.study(study.url))
      }
    )
  }

  def newStudy = silhouette.UserAwareAction.async { implicit request =>
    val f = StudyForm.form
    Future.successful(Ok(views.html.newStudy(request.identity, f)))
  }

  def decline = silhouette.UserAwareAction { implicit request =>
    Ok(views.html.decline())
  }

  def signOut: Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(pages.routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

}

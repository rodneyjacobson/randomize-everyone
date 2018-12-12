package models

import play.api.libs.json._

case class Fetch(study: String)
object Fetch {
  implicit val fetchWrites = Json.writes[Fetch]
}

case class Service(hook: String, title: String, description: String, id: String, prefetch: Option[Seq[Fetch]])
object Service {
  implicit val serviceWrites = Json.writes[Service]
}

case class Services(services: Seq[Service])
object Services {
  implicit val servicesWrites = Json.writes[Services]
}

case class FhirAuth(id: String)
object FhirAuth {
  implicit val fhirAuthReads = Json.reads[FhirAuth]
}

case class ServiceRequest(hook: String, hookInstance: String, fhirServer: Option[String], fhirAuthorization: Option[FhirAuth], context: Option[JsValue], preFetch: Option[JsValue])
object ServiceRequest {
  implicit val serviceRequestReads = Json.reads[ServiceRequest]
}

case class Link(label: String, url: String, `type`: String, appContext: Option[String])
object Link {
  implicit val linkWrites = Json.writes[Link]
}

case class Action(`type`: String, description: String, resource: Option[JsValue])
object Action {
  implicit val actionWrites = Json.writes[Action]
}

case class Suggestion(label: String, uuid: Option[String], actions: Option[Seq[Action]])
object Suggestion {
  implicit val suggestionWrites = Json.writes[Suggestion]
}

case class Source(label: String, url: Option[String] = None, icon: Option[String] = None)
object Source {
  implicit val sourceWrites = Json.writes[Source]
}

case class Card(summary: String, detail: Option[String], indicator: String, source: Source, suggestion: Option[Seq[Suggestion]] = None, selectionBehavior: Option[String] = None, links: Option[Link] = None)
object Card {
  implicit val cardWrites = Json.writes[Card]
}

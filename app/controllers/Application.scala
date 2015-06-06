package controllers

import javax.inject.Inject

import play.api._
import play.api.libs.ws.{WSResponse, WSRequest, WSClient}
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject() (ws: WSClient) extends Controller {

  val pivotalToken = current.configuration.getString("pivotal.token")
  val pivotalEndpoint = current.configuration.getString("pivotal.endpoint")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def pivotal = Action.async { request =>

    val body : Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded

    val wsReq: WSRequest = ws.url(pivotalEndpoint.get)
      .withHeaders("X-TrackerToken" -> pivotalToken.get)
      .withRequestTimeout(300)

    val futureResponse : Future[WSResponse] = wsReq.get()

    futureResponse.map(response => {
      Ok(response.json.toString())
    })
  }
}

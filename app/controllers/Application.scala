package controllers

import javax.inject.Inject

import play.api._
import play.api.libs.json._
import play.api.libs.ws.{WSResponse, WSRequest, WSClient}
import play.api.mvc._
import play.api.Play.current
import play.api.libs.functional.syntax._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject() (ws: WSClient) extends Controller {

  val pivotalToken = current.configuration.getString("pivotal.token")
  val pivotalEndpoint = current.configuration.getString("pivotal.endpoint")

  val webhookUrl = current.configuration.getString("webhook.url")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def pivotal: Action[AnyContent] = Action.async { request =>

    Logger.debug(webhookUrl.get)

    val body : Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

    if (body.get("text").isEmpty)
      BadRequest("No hay parametros con los que trabajar, checa la documentacion con /p help")

    Logger.debug(body.get("user_name").toString)

    val username: String = body.get("user_name").get(0)

    val params : Array[String] = body.get("text").get(0).split(" ")

    val pivotalRequest: WSRequest = buildPivotalRequest(params, pivotalEndpoint.get)
      .withHeaders("X-TrackerToken" -> pivotalToken.get)
      .withHeaders("Accept" -> "application/json")
      .withRequestTimeout(1000)

    val webhookRequest = ws.url(webhookUrl.get)
      .withRequestTimeout(1000)

    val eventualResponse: Future[WSResponse] = for {
      responsePivotal <- pivotalRequest.get()
      msg = "{\"username\": \"webhookbot\", \"text\": \"Hello @" + username + "! Here's the response: " + responsePivotal.json.toString().replace("\"", "'") + " \", \"icon_emoji\": \":ghost:\", \"link_names\": 1}"
      responseSlack <- webhookRequest.post(Map("payload" -> Seq(msg)))
    } yield responseSlack

    /*
    eventualResponse.recover {
      case e: Exception =>
        val exceptionData = Map("error" -> Seq(e.getMessage))
        ws.url(webhookUrl.get).post(Map("payload" -> Seq("{\"text\": \"Hey @" + username + " there was an error. Please investigate\" , \"link_names\": 1}")))
    }
    */

    eventualResponse.map {
      r => Ok(r.body)
    }

  }

  def buildPivotalRequest(params: Array[String], pivotalEndpoint: String) : WSRequest = params match {

    case Array("projects") => ws.url(pivotalEndpoint + params.mkString("/"))

    case Array("projects", _ ) => ws.url(pivotalEndpoint + params.mkString("/"))

    case Array("projects", _ , "stories") => ws.url(pivotalEndpoint + params.mkString("/"))

    case Array("projects", _ , "stories", _) => ws.url(pivotalEndpoint + params.mkString("/"))

    case Array(_*) => ws.url(pivotalEndpoint + "/me")
  }

}

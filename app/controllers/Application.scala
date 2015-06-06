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

    val body : Map[String, Seq[String]] = request.body.asFormUrlEncoded.get



    if (body.get("text").isEmpty)
      BadRequest("No hay parametros con los que trabajar, checa la documentacion con /p help")

    val params : Array[String] = body.get("text").get(0).split(" ")

    val wsReq: WSRequest = buildRequest(params, pivotalEndpoint.get)
      .withHeaders("X-TrackerToken" -> pivotalToken.get)
      .withRequestTimeout(1000)

    val futureResponse : Future[WSResponse] = wsReq.get()

    futureResponse.map(response => {
      Ok( response.json.toString() )
    })
  }

  def buildRequest(params: Array[String], pivotalEndpoint: String) : WSRequest = params match {

      case Array("projects") => ws.url(pivotalEndpoint + params.mkString("/"))

      case Array("projects", _ ) => ws.url(pivotalEndpoint + params.mkString("/"))

      case Array("projects", _ , "stories") => ws.url(pivotalEndpoint + params.mkString("/"))

      case Array("projects", _ , "stories", _) => ws.url(pivotalEndpoint + params.mkString("/"))

      case Array(_*) => ws.url(pivotalEndpoint + "/me")
  }
}

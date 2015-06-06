package controllers

import javax.inject.Inject

import play.api._
import play.api.libs.ws.{WSResponse, WSRequest, WSClient}
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.Future

class Application @Inject() (ws: WSClient) extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def ticket = Action.async { request =>

    val body = request.body.asFormUrlEncoded.get

    val wsReq: WSRequest = ws.url("")

    val futureResponse : Future[WSResponse] = wsReq.post()

    futureResponse.map(response => {
      Ok()
    })
  }

}

package controllers

import javax.inject._

import models.CounterRepository
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class CounterController @Inject()(cr: CounterRepository, cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getCounter = Action.async { implicit request =>
    cr.getCounter.map(x =>
      Ok(views.html.hello(x.getOrElse(0).toString))
    )
  }

  def incrementCounter = Action.async { implicit request =>
    cr.incrementCounter.flatMap(_ => cr.getCounter).map(x =>
      Ok(views.html.hello(x.getOrElse(0).toString))
    )
  }
}

package controllers

import javax.inject._

import models.CounterRepository
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class CounterController @Inject()(cr: CounterRepository, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getCounter = Action.async { implicit request =>
    cr.getCounter.map(x =>
      Ok(views.html.hello(x.toString))
    )
  }

  def incrementCounter = Action.async { implicit request =>
    cr.incrementCounter.flatMap(_ => cr.getCounter).map(x =>
      Ok(views.html.hello(x.toString))
    )
  }
}

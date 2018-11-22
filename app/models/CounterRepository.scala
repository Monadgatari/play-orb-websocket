package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

@Singleton
class CounterRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CounterTable(tag: Tag) extends Table[Counter](tag, "counters") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def x = column[Int]("x")

    def * = (id, x) <> ((Counter.apply _).tupled, Counter.unapply)
  }

  private val counters = TableQuery[CounterTable]

  def getCounter = db.run {
    for {
      xs <- counters.filter(_.id === 1L).map(_.x).result
    } yield xs.head

  }

  def incrementCounter = db.run {
    val q = counters.filter(_.id === 1L).map(_.x)
    (for {
      xs <- q.forUpdate.result
      _ <- q.update(xs.head + 1)
    } yield ()).transactionally
  }
}

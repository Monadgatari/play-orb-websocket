# Play example project
## Description
Backend : Scala with Slick (PostreSQL/MariaDB), ...<br/>
Frontend : Typescript with ...<br/>
Shared Libraries : SocketIO, ...<br/>
Static : Sass + play html templates

## Create initial project
`sbt new playframework/play-scala-seed.g8`

Note : it will ask for the project name (which will be the name of the folder) and organisation name.

Exemple : `play-orb-websocket` and `ch.monadgatari`

## Compile / Run
Go to the created folder :
`cd project-folder`

launch sbt :
`sbt`

now that you are in the sbt prompt, launch the app with :
```shell
run
```
The app will be compiled when you send a request if any file has changed since the last compilation. Alternatively, you can use the following to re-compile as soon as a file is saved (instead of waiting for a request) :
```shell
~run
```
I wouldn't recommend the second option with intelliJ IDEA since it continusly saves files automatically while you type.

## Create basic hello world page
in a new `hello.scala.html` file in `app/views` write :
```scala
@(name: String)
@main("Hello") {
    <section id="top">
        <div class="wrapper">
            <h1>Hello, @name!</h1>
        </div>
    </section>
}
```

This will create a new template that calls the `main.scala.html` template with the specific title and content to generate a page.

in the `app/controllers/HomeController.scala` file, add the new method :
```scala
def hello(name: String) = Action {
  Ok(views.html.hello(name))
}
```

This method will call invoque the template and send back the result when requested.

And finally add the route that will call the method in `conf/routes` :
```
GET     /hello/:name        controllers.HomeController.hello(name: String)
```

This is all you need, load http://localhost:9000/hello/YourName to charge the page.

## Create a basic page loading counter using play-slick and postgresql
### PostgreSQL preparation (linux)
Install postgresql on your system. Most linux distribution have it in their repositories.

Then, you have to init the database cluster. The instructions for archlinux are [here](https://wiki.archlinux.org/index.php/PostgreSQL) but I suppose it's the same for most systems.<br/>
Switch to the `postgres` user and type :
```shell
initdb --locale en_US.UTF-8 -E UTF8 -D '/var/lib/postgres/data'
```

Go back to the user that will run play for the next steps.

### Add Slick/PostgreSQL sbt depencies and setup configs
The first step is to create the database we'll use for the app. I'll call it call it `playdb` :
```shell
createdb playdb
```

Then, add the Slick, Slick-Evolution and PostreSQL JDBC driver dependencies to the sbt project in `build.sbt` :
```sbt
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"
)
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5"
```
Note : You can check the available / compatible versions for play [here](https://github.com/playframework/play-slick) and for postgresql [here](https://jdbc.postgresql.org/index.html).

Finally, add the slick configuration in `conf/application.conf` :
```conf
slick.dbs.default.profile="slick.jdbc.PostgresProfile$"
slick.dbs.default.db.dataSourceClass = "slick.jdbc.DatabaseUrlDataSource"
slick.dbs.default.db.properties.driver = "org.postgresql.Driver"
slick.dbs.default.db.properties.url="jdbc:postgresql://localhost:5432/playdb" # ?currentSchema=play"
#play.evolutions.db.default.schema="play" # if you want to use a specific schema for evolution's table
#slick.dbs.default.db.properties.user = "postgres"
#slick.dbs.default.db.properties.password = ""
#slick.dbs.default.db.connectionTestQuery = "SELECT 1" # workaround for bug: "Failed to execute isValid()"
```

If the configuration is properly done, you should still be able to load the hello world page (otherwise it will display an error).
### Create Table + Model + Create Slick link for counters
To create a new table, you'll first want to create a database evolution script `conf/evolutions/default/1.sql` to create the table in the database. Here is an example script for a "counter" table with a single counter element inserted :
```SQL
# --- !Ups

CREATE TABLE counters(id SERIAL PRIMARY KEY, x integer NOT NULL);
INSERT INTO counters(x) VALUES(0);

# --- !Downs

DROP TABLE counters;
```
Any future script will have a higher identifier (`2.sql`, `3.sql`, ...). The up parts are applied at installation on a new system, and the down parts are used to account for updates (from a git merge for example) of a script. (the script that were applied are stored in a table to be able to unapply them when needed)

Then, you'll want to create the counter model in the app, corresponding to a line of the table. This will be a case class that you would probably place in `app/models/Counter.scala` :
```Scala
package models

case class Counter(id: Long, x: Int)
```
Yes, that's it for now. For some classes, to be able to communicate with the frontend, it can be usefull to add this :
```scala
import play.api.libs.json._

object Counter {
  implicit val counterFormat: OFormat[Counter] = Json.format[Counter]
}
```

Now, the last step is to create the database to model association. For that create the `app/models/CounterRepository.scala` file, and we'll put the following content :
```scala
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
  
  // TODO : Add queries here...
}
```

Here, it's probably the most complex part. Let me explain :
- Due to the way depency injenction works, this has to be a Singleton class and not a scala object.
The depency injection provides us with the database configuration needed.
- The CounterTable class is a class that associates the table columns with the models values.
- The columns are defined by functions that returns a column of some type given it's id and some options.
- The * function provides the columns to scala model bi-directionnal convertion information
- private val counters creates a starting point for database queries to this table.

Now, we can add some simple queries to the CounterRepository class that reads and increment the single counter that we have in the table :
```scala
  def getCounter: Future[Option[Int]] = db.run {
      counters.filter(_.id === 1L).map(_.x).result.headOption
  }

  def incrementCounter: Future[Unit] = db.run {
    val q = counters.filter(_.id === 1L).map(_.x)
    (for {
      xs <- q.forUpdate.result if xs.nonEmpty
      _ <- q.update(xs.head + 1)
    } yield ()).transactionally
  }
```

### Create route to access/update the value
Here, we'll need a controller that has access to the `CounterRepository` singleton class. For that, we'll create `app/controllers/CounterController.scala` and use injection :
```scala
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
      Ok(views.html.hello(x.getOrElse(0).toString))
    )
  }

  def incrementCounter = Action.async { implicit request =>
    cr.incrementCounter.flatMap(_ => cr.getCounter).map(x =>
      Ok(views.html.hello(x.getOrElse(0).toString))
    )
  }
}
```
Here we use the hello view since we did not implement an other view yet. The action are async here because of the database request.

The last thing is to register the routes in `conf/routes` :
```
# Counter routes
GET     /counter                     controllers.CounterController.getCounter
GET     /increment                   controllers.CounterController.incrementCounter
```

you can now try to load and refresh http://localhost:9000/counter and http://localhost:9000/increment multiple times.

## Various optimisations :
To add IntelliJ IDEA error hyperlinks, enter the following line in `conf/application.conf`:
```
play.editor="http://localhost:63342/api/file/?file=%s&line=%s"
```

To disable host filtering in your play application (not recommended while developing), add the following to `conf/application.conf` :
```
# Disable filters :
play.filters.hosts {
  allowed = ["."]
}
```

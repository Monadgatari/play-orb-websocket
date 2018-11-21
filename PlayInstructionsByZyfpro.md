# Play example project
#### Description
Backend : Scala with Slick (PostreSQL/MariaDB), ...
Frontend : Typescript with ...
Shared Libraries : SocketIO, ...
Static : Sass + play html templates

#### Create initial project
`sbt new playframework/play-scala-seed.g8`

Note : it will ask for the project name (which will be the name of the folder) and organisation name.

Exemple :
`play-orb-websocket`
and
`ch.monadgatari`

#### Compile / Run
Go to the created folder :
`cd project-folder`

launch sbt :
`sbt`

now that you are in the sbt prompt, launch the app with automatic re-compilation when receiving request if a file changed :
`run`
alternatively, you can use the following to re-compile everytime you save a file even without requests:
`~run`

#### Create basic hello world page
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

This will call the main the `main.scala.html` template with the specific title and content.

in the `app/controllers/HomeController.scala` file, add the new method :
```scala
def hello(name: String) = Action {
  Ok(views.html.hello(name))
}
```

And add a reference to the method in `conf/routes` :
`GET     /hello/:name        controllers.HomeController.hello(name: String)`

This is all you need, load http://localhost:9000/hello/YourName to charge the page.

#### 

#### Various optimisations :
To add IntelliJ IDEA error hyperlinks, enter the following line in application.conf:
`play.editor="http://localhost:63342/api/file/?file=%s&line=%s"`
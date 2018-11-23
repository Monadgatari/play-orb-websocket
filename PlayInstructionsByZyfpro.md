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
```bash
run
```
The app will be compiled when you send a request if any file has changed since the last compilation. Alternatively, you can use the following to re-compile as soon as a file is saved (instead of waiting for a request) :
```bash
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
**TODO**

## Various optimisations :
To add IntelliJ IDEA error hyperlinks, enter the following line in application.conf:
```
play.editor="http://localhost:63342/api/file/?file=%s&line=%s"
```

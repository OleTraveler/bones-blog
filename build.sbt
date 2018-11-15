sourceManaged in Compile := file("content")
enablePlugins(TutPlugin)


libraryDependencies ++= Seq(
  "io.argonaut" %% "argonaut" % "6.2.2",
)

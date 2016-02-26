lazy val commonSetting = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.7"
)

lazy val test = (project in file("./"))
  .settings(commonSetting)
  .settings(
    name := "stanby-ats-test",
    libraryDependencies ++= Seq(
      "org.scalaz"           %% "scalaz-core"     % "7.1.5",
      "org.scalaz"           %% "scalaz-effect"   % "7.2.0",
      "org.scalatest"        %% "scalatest"       % "2.1.5"        % "test",
      "org.mockito"           % "mockito-core"    % "1.10.19"      % "test"
    )
  )

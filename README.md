sbt-jenkins-manager
===================

Drive Jenkins from within SBT

Building


git clone git@github.com:kalmanb/sbt-jenkins-manager.git
cd sbt-jenkins-manager
sbt
> publish-local

Include in a project:
In your "project/plugins.sbt" add the following line:
addSbtPlugin("org.kalmanb" % "sbt-jenkins-manager" % "0.1.0-SNAPSHOT")

Add your jenkins url to your project settings:

lazy val name = Project(
   ...
   settings = Seq(
     ...
     jenkinsBaseUrl := "http://jenkins.hostname.com/"
   )
)

Usage:
sbt
> jenkins-copy-view src dest
> jenkins-build-all-jobs-in-view viewName
> jenkins-change-job-branch jobName branchName
> jenkins-delete-view-and-jobs viewName


package com.kalmanb.sbt

import scala.xml.Elem
import scala.xml.transform.RewriteRule

import dispatch._
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._
import sbt.complete.Parser

object JenkinsPlugin extends JenkinsPluginTrait
trait JenkinsPluginTrait extends Plugin {

  // Settings for Build.scala
  val jenkinsBaseUrl = SettingKey[String]("jenkinsBaseUrl", "The base URL for your Jenkins Server, eg http://jenkins.foo.com")

  // Tasks
  val jenCopyJob = InputKey[Unit]("jenkinsCopyJob",
    "<scr> <dest> create a copy of an existing job")
  val jenBuildJob = InputKey[Unit]("jenkinsBuildJob",
    "<jobName> start a build for a Job")
  val jenDeleteJob = InputKey[Unit]("jenkinsDeleteJob",
    "<jobName> delete Job from Jenkins")
  val jenDeleteJobRegex = InputKey[Unit]("jenkinsDeleteJobRegex",
    "<jobRegex> delete Job from Jenkins")
  val jenChangeJobBranch = InputKey[Unit]("jenkinsChangeJobBranch",
    "<jobName> <branch> change a jobs git branch setting")
  val jenChangeViewBranch = InputKey[Unit]("jenkinsChangeViewBranch",
    "<viewName> <branch> change all jobs in the view to a new git branch setting")
  val jenChangeJobsBranch = InputKey[Unit]("jenkinsChangeJobsBranch",
    "<regex> <branch> change all jobs that match a regex to a new git branch setting")
  val jenCreateView = InputKey[Unit]("jenkinsCreateView",
    "<name> create a new view")
  val jenCopyView = InputKey[Unit]("jenkinsCopyView",
    "<src> <dst> [prefix] creates a new view with name <dst> and duplicates all jobs in <src>. Prefix is for the new jobs, it's optional and defaults to <dst>")
  val jenAddJobToView = InputKey[Unit]("jenkinsAddJobToView",
    "<jobName> <viewName> create a new view")
  val jenDeleteView = InputKey[Unit]("jenkinsDeleteView",
    "<name> deletes the view, does NOT delete the jobs in the view")
  val jenDeleteViewAndJobs = InputKey[Unit]("jenkinsDeleteViewAndJobs",
    "<name> deletes the view and deletes all the jobs in the view")
  val jenBuildAllJobsInView = InputKey[Unit]("jenkinsBuildAllJobsInView",
    "<name> queues the build of all jobs")
  val jenSetWipeoutWorkspaceView = InputKey[Unit]("jenkinsSetWipeoutWorkspaceView",
    "<view> <true|false> [ignore,projects] - changes the setting for wipeout workspace in the specified view")
  val jenChangeThrottleCategories = InputKey[Unit]("jenkinsChangeViewThrottleCats",
    "<view> <cat1,cat2,cat3> [ignore,projects] -changes the setting for wipeout workspace in the specified view")

  lazy val jenkinsSettings = Seq(
    jenCopyJob <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).copyJob(args.head, args(1))),
    jenBuildJob <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).buildJob(args.head)),
    jenDeleteJob <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).deleteJob(args.head)),
    jenDeleteJobRegex <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).deleteJobRegex(args.head)),
    jenChangeJobBranch <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).changeJobGitBranch(args.head, args(1))),
    jenChangeViewBranch <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).changeViewGitBranch(args.head, args(1))),
    jenChangeJobsBranch <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).changeJobsGitBranch(args.head, args(1))),
    jenCreateView <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).createView(args.head)),
    jenCopyView <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).copyView(args.head, args(1))),
    jenAddJobToView <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).addJobToView(args.head, args(1))),
    jenDeleteView <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).deleteView(args.head)),
    jenDeleteViewAndJobs <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).deleteViewAndJobs(args.head)),
    jenBuildAllJobsInView <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).buildAllJobsInView(args.head)),
    jenSetWipeoutWorkspaceView <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).setWipeOutWorkspaceForView(args)))

  def validateArgs(args: Seq[_], size: Int) {
    if (args.size < size) throw new IllegalArgumentException("expected %s args, got %s".format(size, args.size))
  }

  def jenkinsTask(numberOfParms: Int, f: (String, Seq[String]) ⇒ Unit) = {
    inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (baseUrl, args) ⇒
        {
          validateArgs(args, numberOfParms);
          f(baseUrl, args)
        }
      }
    }
  }

  case class Jenkins(baseUrl: String) {

    def createView(view: String): Unit =
      logServerNotFound(() ⇒ {
        val params = Map("name" -> view, "mode" -> "hudson.model.ListView",
          "json" -> "{\"name\": \"%s\", \"mode\": \"hudson.model.ListView\"}".format(view))
        Http(dispatch.url(baseUrl + "/createView") << params)()
      })

    def deleteView(view: String) = {
      logViewNotFound(() ⇒ Http(dispatch.url(baseUrl + "/view/%s/doDelete".format(view)).POST)(), view)
    }

    def getViewConfig(view: String) =
      logViewNotFound(() ⇒ Http(dispatch.url(baseUrl + "/view/%s/config.xml".format(view)) OK as.xml.Elem)(), view)

    def addJobToView(job: String, view: String): Unit =
      logViewNotFound(() ⇒ Http(dispatch.url(baseUrl + "/view/%s/addJobToView".format(view)) << Map("name" -> job) OK as.String)(), view)

    def getJobConfig(job: String) =
      logJobNotFound(() ⇒ Http(dispatch.url(baseUrl + "/job/%s/config.xml".format(job)) OK as.xml.Elem)(), job)

    def updateJobConfig(job: String, config: Seq[scala.xml.Node]) =
      logJobNotFound(() ⇒ Http(dispatch.url(baseUrl + "/job/%s/config.xml".format(job)).POST.setBody(config.mkString) OK as.String)(), job)

    def updateJob(job: String, f: (Seq[scala.xml.Node]) ⇒ Seq[scala.xml.Node]): Unit = {
      val config = Jenkins(baseUrl).getJobConfig(job)
      Jenkins(baseUrl).updateJobConfig(job, f(config))
      println("Updated " + job)
    }

    def changeJobGitBranch(job: String, newBranch: String) {
      val config = getJobConfig(job)

      val updated = new RewriteRule {
        override def transform(n: scala.xml.Node): Seq[scala.xml.Node] = n match {
          case Elem(prefix, "hudson.plugins.git.BranchSpec", attribs, scope, child @ _*) ⇒ Elem(prefix, "hudson.plugins.git.BranchSpec", attribs, scope, <name>{ newBranch }</name>: _*)
          case elem: Elem ⇒ elem copy (child = elem.child flatMap (this transform))
          case other ⇒ other
        }
      } transform config

      updateJobConfig(job, updated)
    }

    def setWipeOutWorkspaceForView(args: Seq[String]): Unit = {
      val view = args.head
      val wipeOutWorkspace = args(1).toBoolean
      val ignoreList = if (args.size > 2) Some(args(2)) else None
      val ignoredProjects = ignoreList.getOrElse("").split(",").map(_.trim)
      getJobsInView(view).diff(ignoredProjects).foreach(setWipeOutWorkspaceForJob(_, wipeOutWorkspace))
    }

    def setWipeOutWorkspaceForJob(job: String, wipeOutWorkspace: Boolean): Unit = {
      val config = getJobConfig(job)
      val updated = new RewriteRule {
        override def transform(n: scala.xml.Node): Seq[scala.xml.Node] = n match {
          case Elem(prefix, "wipeOutWorkspace", attribs, scope, child @ _*) ⇒ <wipeOutWorkspace>{ wipeOutWorkspace }</wipeOutWorkspace>
          case elem: Elem ⇒ elem copy (child = elem.child flatMap (this transform))
          case other ⇒ other
        }
      } transform config
      updateJobConfig(job, updated)
      println("Updated " + job + " with wipeOutWorkspace to " + wipeOutWorkspace)
    }

    def changeViewGitBranch(view: String, newBranch: String): Unit = {
      getJobsInView(view).foreach(changeJobGitBranch(_, newBranch))
    }

    def changeJobsGitBranch(regex: String, newBranch: String): Unit = {
      val pattern = new scala.util.matching.Regex(regex)
      getAllJobs().filter(
        job ⇒ pattern findFirstIn job isDefined).foreach { job ⇒
          println("Changing branch to " + newBranch + " for job " + job + ".")
          changeJobGitBranch(job, newBranch)
        }
    }

    def createJob(job: String, config: Seq[scala.xml.Node]): Unit = {
      logServerNotFound(() ⇒
        Http(dispatch.url(baseUrl + "/createItem".format(job)).POST
          .setBody(config.mkString).setHeader("Content-Type", "text/xml") <<? Map("name" -> job) OK as.String)())
    }

    def copyJob(src: String, dst: String): Unit =
      logServerNotFound(() ⇒ {
        val params = Map("name" -> dst, "mode" -> "copy", "from" -> src)
        Http(dispatch.url(baseUrl + "/createItem") << params)()
        // Seems to be a bug in Jenkins 1.525 when you copy a job it doesn't show the build button in the UI
        // This enables the build button
        disableJob(dst)
        enableJob(dst)
      })

    def disableJob(job: String) = {
      logJobNotFound(() ⇒ Http(dispatch.url(baseUrl + "/job/%s/disable".format(job)).POST)(), job)
    }

    def enableJob(job: String) = {
      logJobNotFound(() ⇒ Http(dispatch.url(baseUrl + "/job/%s/enable".format(job)).POST)(), job)
    }

    def buildJob(job: String): Unit = {
      println("Building Job " + job)
      logJobNotFound(() ⇒ Http(dispatch.url(baseUrl + "/job/%s/build".format(job)).POST)(), job)
    }

    def deleteJob(job: String): Unit = {
      println("Deleting Job " + job)
      logJobNotFound(() ⇒ Http(dispatch.url(baseUrl + "/job/%s/doDelete".format(job)).POST)(), job)
    }

    def deleteJobRegex(regex: String): Unit = {
      val pattern = new scala.util.matching.Regex(regex)
      getAllJobs().filter(
        job ⇒ pattern findFirstIn job isDefined).foreach { job ⇒
          deleteJob(job)
        }
    }

    def buildAllJobsInView(view: String) {
      getJobsInView(view).foreach(buildJob)
    }

    def getAllJobs() = {
      logNotFound(() ⇒ {
        val config = Http(dispatch.url(baseUrl + "/api/xml") OK as.xml.Elem)()
        val nodes = config \\ "job" \\ "name"
        nodes.map(_.text)
      }, "Could not find jobs on server %s".format(baseUrl))
    }

    def getJobsInView(view: String) = {
      logNotFound(() ⇒ {
        val config = Http(dispatch.url(baseUrl + "/view/%s/config.xml".format(view)) OK as.xml.Elem)()
        val nodes = config \\ "jobNames" \\ "string"
        nodes.map(_.text)
      }, "Could not find view %s on server %s".format(view, baseUrl))
    }

    def logNotFound[T](f: () ⇒ T, message: ⇒ String): T = {
      try {
        f()
      } catch {
        case e if (e.getMessage.contains("404")) ⇒ { println(message + "\n"); throw e }
        case e                                   ⇒ throw e
      }
    }

    def logViewNotFound[T](f: () ⇒ T, view: String): T = logNotFound(f, "Could not find view %s on server %s".format(view, baseUrl))

    def logJobNotFound[T](f: () ⇒ T, job: String): T = logNotFound(f, "Could not find job %s on server %s".format(job, baseUrl))

    def logServerNotFound[T](f: () ⇒ T): T = logNotFound(f, "Could not connect to server %s".format(baseUrl))

    def copyView(src: String, dst: String) { copyView(src, dst, dst + "_") }

    def copyView(src: String, dst: String, prefix: String) {
      createView(dst)
      val originalJobs = getJobsInView(src)
      println(originalJobs)
      originalJobs.foreach(job ⇒ copyJob(job, prefix + job))
      originalJobs.foreach(job ⇒ addJobToView(prefix + job, dst))
    }

    def deleteViewAndJobs(view: String) {
      getJobsInView(view).foreach(deleteJob)
      deleteView(view)
    }
  }
}

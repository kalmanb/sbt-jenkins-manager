package org.kalmanb.sbt

import sbt._
import sbt.complete.Parser
import complete.DefaultParsers._
import Keys._
import dispatch._
import scala.xml.Document
import scala.xml.transform.RewriteRule
import scala.xml.Elem

object JenkinsPlugin extends JenkinsPluginTrait
trait JenkinsPluginTrait extends Plugin {

  // Settings for Build.scala
  val jenkinsBaseUrl = SettingKey[String]("jenkinsBaseUrl", "The base URL for your Jenkins Server, eg http://jenkins.foo.com")

  // Tasks
  val jenCopyJob = InputKey[Unit]("jenkins-copy-job", "<scr> <dest> create a copy of an existing job")
  val jenBuildJob = InputKey[Unit]("jenkins-build-job", "<job-name> start a build for a Job")
  val jenDeleteJob = InputKey[Unit]("jenkins-delete-job", "<job-name> delete Job from Jenkins")
  val jenChangeJobBranch = InputKey[Unit]("jenkins-change-job-branch", "<job-name> <branch> change a jobs git branch setting")
  val jenChangeViewBranch = InputKey[Unit]("jenkins-change-view-branch", "<view-name> <branch> change all jobs in the view to a new git branch setting")
  val jenChangeJobsBranch = InputKey[Unit]("jenkins-change-jobs-branch", "<regex> <branch> change all jobs that match a regex to a new git branch setting")

  val jenCreateView = InputKey[Unit]("jenkins-create-view", "<name> create a new view")
  val jenCopyView = InputKey[Unit]("jenkins-copy-view", "<src> <dst> [prefix] creates a new view with name <dst> and duplicates all jobs in <src>. Prefix is for the new jobs, it's optional and defaults to <dst>")
  val jenAddJobToView = InputKey[Unit]("jenkins-add-job-to-view", "<job-name> <view-name> create a new view")
  val jenDeleteView = InputKey[Unit]("jenkins-delete-view", "<name> deletes the view, does NOT delete the jobs in the view")
  val jenDeleteViewAndJobs = InputKey[Unit]("jenkins-delete-view-and-jobs", "<name> deletes the view and deletes all the jobs in the view")
  val jenBuildAllJobsInView = InputKey[Unit]("jenkins-build-all-jobs-in-view", "<name> queues the build of all jobs")
  val jenSetWipeoutWorkspaceView = InputKey[Unit]("jenkins-set-wipeout-workspace-view", "<view> <true|false> [ignore,projects] - changes the setting for wipeout workspace in the specified view")
  val jenChangeThrottleCategories = InputKey[Unit]("jenkins-change-view-throttle-cats", "<view> <cat1,cat2,cat3> [ignore,projects] -changes the setting for wipeout workspace in the specified view")

  lazy val jenkinsSettings = Seq(
    jenCopyJob <<= inputTask { (argTask) ⇒
      (baseDirectory, jenkinsBaseUrl, argTask) map { (baseDirectory, host, args) ⇒
        validateArgs(args, 2); Jenkins(host).copyJob(args.head, args(1))
      }
    },
    jenBuildJob <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 1); Jenkins(host).buildJob(args.head)
      }
    },
    jenDeleteJob <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 1); Jenkins(host).deleteJob(args.head)
      }
    },
    jenChangeJobBranch <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 2); Jenkins(host).changeJobGitBranch(args.head, args(1))
      }
    },
    jenChangeViewBranch <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 2); Jenkins(host).changeViewGitBranch(args.head, args(1))
      }
    },
    jenChangeJobsBranch <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 2); Jenkins(host).changeJobsGitBranch(args.head, args(1))
      }
    },
    jenCreateView <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 1); Jenkins(host).createView(args.head)
      }
    },
    jenCopyView <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 2); Jenkins(host).copyView(args.head, args(1))
      }
    },
    jenAddJobToView <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 2); Jenkins(host).addJobToView(args.head, args(1))
      }
    },
    jenDeleteView <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 1); Jenkins(host).deleteView(args.head)
      }
    },
    jenDeleteViewAndJobs <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 1); Jenkins(host).deleteViewAndJobs(args.head)
      }
    },
    jenBuildAllJobsInView <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        validateArgs(args, 1); Jenkins(host).buildAllJobsInView(args.head)
      }
    },
    jenSetWipeoutWorkspaceView <<= inputTask { (argTask) ⇒
      (jenkinsBaseUrl, argTask) map { (host, args) ⇒
        val ignoreList = if(args.size > 2) Some(args(2)) else None
        validateArgs(args, 2); Jenkins(host).setWipeOutWorkspaceForView(args.head, args(1), ignoreList)
      }
    },
    jenChangeThrottleCategories <<= inputTask { (argTask) =>
      (jenkinsBaseUrl, argTask) map { (host, args) => {
        val ignoreList = if(args.size > 2) Some(args(2)) else None
        validateArgs(args, 2); Jenkins(host).changeThrottleCategoriesView(args.head, args(1), ignoreList)
      }}
    }
    )
  def validateArgs(args: Seq[_], size: Int) {
    if (args.size < size) throw new IllegalArgumentException("expected %s args, got %s".format(size, args.size))
  }

  case class Jenkins(baseUrl: String) {

    def createView(view: String): Unit =
      logServerNotFound(() => {
        val params = Map("name" -> view, "mode" -> "hudson.model.ListView",
          "json" -> "{\"name\": \"%s\", \"mode\": \"hudson.model.ListView\"}".format(view))
        Http(dispatch.url(baseUrl + "/createView") << params)()
      })

    def deleteView(view: String) = {
      logViewNotFound(() => Http(dispatch.url(baseUrl + "/view/%s/doDelete".format(view)).POST)(), view)
    }

    def getViewConfig(view: String) =
      logViewNotFound(() => Http(dispatch.url(baseUrl + "/view/%s/config.xml".format(view)) OK as.xml.Elem)(), view)

    def addJobToView(job: String, view: String): Unit =
      logViewNotFound(() => Http(dispatch.url(baseUrl + "/view/%s/addJobToView".format(view)) << Map("name" -> job) OK as.String)(), view)

    def getJobConfig(job: String) =
      logJobNotFound(() => Http(dispatch.url(baseUrl + "/job/%s/config.xml".format(job)) OK as.xml.Elem)(), job)

    def updateJobConfig(job: String, config: Seq[scala.xml.Node]) =
      logJobNotFound(() => Http(dispatch.url(baseUrl + "/job/%s/config.xml".format(job)).POST.setBody(config.mkString) OK as.String)(), job)

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

    def setWipeOutWorkspaceForView(view: String, wipeOutWorkspace: String, ignoreList: Option[String]): Unit = {
      val ignoredProjects = ignoreList.getOrElse("").split(",").map(_.trim)
      getJobsInView(view).diff(ignoredProjects).foreach(setWipeOutWorkspaceForJob(_, wipeOutWorkspace.toBoolean))
    }

    def setWipeOutWorkspaceForJob(job:String, wipeOutWorkspace: Boolean): Unit = {
      val config = getJobConfig(job)
      val updated = new RewriteRule {
        override def transform(n: scala.xml.Node): Seq[scala.xml.Node] = n match {
          case Elem(prefix, "wipeOutWorkspace", attribs, scope, child @ _*) ⇒ <wipeOutWorkspace>{wipeOutWorkspace}</wipeOutWorkspace>
          case elem: Elem ⇒ elem copy (child = elem.child flatMap (this transform))
          case other ⇒ other
        }
      } transform config
      updateJobConfig(job, updated)
      println("Updated "+ job +" with wipeOutWorkspace to " + wipeOutWorkspace)
    }

    def changeThrottleCategoriesView(view: String, categoryList: String, ignoreList: Option[String]): Unit = {
      val ignoredProjects = ignoreList.getOrElse("").split(",").map(_.trim)
      val categories = categoryList.split(",").map(_.trim)
      getJobsInView(view).diff(ignoredProjects).foreach(changeThrottleCategoriesJob(_, categories))
    }

    // TODO - currently only replaces, should insert if not found
    def changeThrottleCategoriesJob(job: String, categories: Seq[String]): Unit = {
      val config = getJobConfig(job)
      val settings =
        <wrapper>
          <maxConcurrentPerNode>0</maxConcurrentPerNode>
          <maxConcurrentTotal>0</maxConcurrentTotal>
          <categories>
          {categories.map(cat => <string>{cat}</string>)}
        </categories>
        <throttleEnabled>true</throttleEnabled>
        <throttleOption>category</throttleOption>
      </wrapper>
    val updated = new RewriteRule {
      override def transform(n: scala.xml.Node): Seq[scala.xml.Node] = n match {
        case Elem(prefix, "hudson.plugins.throttleconcurrents.ThrottleJobProperty", attribs, scope, child @ _*) ⇒
        Elem(prefix, "hudson.plugins.throttleconcurrents.ThrottleJobProperty", attribs, scope, settings \ "_":_*)
        case elem: Elem ⇒ elem copy (child = elem.child flatMap (this transform))
        case other ⇒ other
      }
    } transform config
    updateJobConfig(job, updated)
    println("Updated "+ job +" changing categories to " + categories.mkString(","))
    }

    def changeViewGitBranch(view: String, newBranch: String):Unit = {
      getJobsInView(view).foreach(changeJobGitBranch(_, newBranch))
    }

    def changeJobsGitBranch(regex: String, newBranch: String): Unit = {
      val pattern = new scala.util.matching.Regex(regex)
      getAllJobs().filter(
        job ⇒ pattern findFirstIn job isDefined
      ).foreach { job ⇒
        println("Changing branch to " + newBranch + " for job " + job + ".")
        changeJobGitBranch(job, newBranch)
      }
    }

    def createJob(job: String, config: Seq[scala.xml.Node]): Unit = {
      logServerNotFound(() =>
          Http(dispatch.url(baseUrl + "/createItem".format(job)).POST
            .setBody(config.mkString).setHeader("Content-Type", "text/xml") <<? Map("name" -> job) OK as.String)()
          )
    }

    def copyJob(src: String, dst: String): Unit =
      logServerNotFound(() => {
        val params = Map("name" -> dst, "mode" -> "copy", "from" -> src)
        Http(dispatch.url(baseUrl + "/createItem") << params)()
      })

    def buildJob(job: String): Unit =
      logJobNotFound(() => Http(dispatch.url(baseUrl + "/job/%s/build".format(job)))(), job)

    def deleteJob(job: String): Unit =
      logJobNotFound(() => Http(dispatch.url(baseUrl + "/job/%s/doDelete".format(job)).POST)(), job)

    def buildAllJobsInView(view: String) {
      getJobsInView(view).foreach(buildJob)
    }

    def getAllJobs() = {
      logNotFound(() => {
        val config = Http(dispatch.url(baseUrl + "/api/xml") OK as.xml.Elem)()
        val nodes = config \\ "job" \\ "name"
        nodes.map(_.text)
      }, "Could not find jobs on server %s".format(baseUrl))
    }

    def getJobsInView(view: String) = {
      logNotFound(() => {
        val config = Http(dispatch.url(baseUrl + "/view/%s/config.xml".format(view)) OK as.xml.Elem)()
        val nodes = config \\ "jobNames" \\ "string"
        nodes.map(_.text)
      }, "Could not find view %s on server %s".format(view, baseUrl))
    }

    def logNotFound[T] (f: () => T, message: => String):T = {
      try {
        f()
      } catch {
        case e if(e.getMessage.contains("404")) => {println(message + "\n"); throw e }
          case e => throw e
      }
    }

    def logViewNotFound[T] (f:() => T, view: String):T = logNotFound(f, "Could not find view %s on server %s".format(view, baseUrl))

    def logJobNotFound[T] (f:() => T, job: String):T = logNotFound(f, "Could not find job %s on server %s".format(job, baseUrl))

    def logServerNotFound[T] (f:() => T):T = logNotFound(f, "Could not connect to server %s".format(baseUrl))

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

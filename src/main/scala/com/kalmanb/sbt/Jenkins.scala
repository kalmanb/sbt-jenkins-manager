package com.kalmanb.sbt

import dispatch._
import scala.xml.Elem
import scala.xml.transform.RewriteRule

object Jenkins {
  def apply(baseUrl: String) = new Jenkins(baseUrl)
}
class Jenkins(baseUrl: String) {

  def createView(view: String, hostUrl: String = baseUrl): Unit =
    logServerNotFound(() ⇒ {
      val params = Map("name" -> view, "mode" -> "hudson.model.ListView",
        "json" -> "{\"name\": \"%s\", \"mode\": \"hudson.model.ListView\"}".format(view))
      Http(dispatch.url(hostUrl + "/createView") << params)()
    })

  def deleteView(view: String) = {
    logViewNotFound(() ⇒ Http(dispatch.url(baseUrl + "/view/%s/doDelete".format(view)).POST)(), view)
  }

  def getViewConfig(view: String) =
    logViewNotFound(() ⇒ Http(dispatch.url(baseUrl + "/view/%s/config.xml".format(view)) OK as.xml.Elem)(), view)

  def addJobToView(job: String, view: String, hostUrl: String = baseUrl): Unit =
    logViewNotFound(() ⇒ Http(dispatch.url(hostUrl + "/view/%s/addJobToView".format(view)) << Map("name" -> job) OK as.String)(), view)

  def getJobConfig(job: String, hostUrl:String = baseUrl) =
    logJobNotFound(() ⇒ Http(dispatch.url(hostUrl + "/job/%s/config.xml".format(job)) OK as.xml.Elem)(), job)

  def updateJobConfig(job: String, config: Seq[scala.xml.Node]) =
    logJobNotFound(() ⇒ Http(dispatch.url(baseUrl + "/job/%s/config.xml".format(job)).POST.setBody(config.mkString) OK as.String)(), job)

  def updateJob(job: String, f: (Seq[scala.xml.Node]) ⇒ Seq[scala.xml.Node]): Unit = {
    val config = Jenkins(baseUrl).getJobConfig(job)
    Jenkins(baseUrl).updateJobConfig(job, f(config))
    println("Updated " + job)
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

  def createJob(job: String, config: Seq[scala.xml.Node], hostUrl: String = baseUrl): Unit = {
    logServerNotFound(() ⇒
      Http(dispatch.url(hostUrl + "/createItem".format(job)).POST
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

  def buildJob(job: String) = {
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

  def getJobsInView(view: String, hostUrl: String = baseUrl) = {
    logNotFound(() ⇒ {
      val config = Http(dispatch.url(hostUrl + "/view/%s/config.xml".format(view)) OK as.xml.Elem)()
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

  def copyViewToOtherServer(srcUrl: String, dstUrl: String, view: String) {
    createView(view, dstUrl)
    val originalJobs = getJobsInView(view, srcUrl)
    println(originalJobs)
    originalJobs.foreach(job ⇒ createJob(job,getJobConfig(job, srcUrl) , dstUrl))
    originalJobs.foreach(job ⇒ addJobToView(job, view, dstUrl))
  }

  def deleteViewAndJobs(view: String) {
    getJobsInView(view).foreach(deleteJob)
    deleteView(view)
  }

  def getJobInfo(job: String): Elem =
    Http(url(baseUrl + "/job/%s/api/xml".format(job)) OK as.xml.Elem)()
}


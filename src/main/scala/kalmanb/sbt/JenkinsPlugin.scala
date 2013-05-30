package kalmanb.sbt

import sbt._
import Keys._
import sbt.TaskKey
import dispatch._
import scala.xml.Document
import scala.xml.transform.RewriteRule
import scala.xml.Elem

object JenkinsPlugin extends JenkinsPluginTrait
trait JenkinsPluginTrait extends Plugin {
  object Keys {
    val copyProject = TaskKey[Unit]("jenCopyProject")
    val jenCreateView = InputKey[Unit]("jenCreateView")
  }
  import Keys._

  val JenkinsBaseUrl = "http://jenkins.movio.co/"

  lazy val jenkinsSettings = Seq(
    copyProject <<= (baseDirectory, name) map { (baseDirectory, name) ⇒
      println(baseDirectory)
    },

    jenCreateView <<= inputTask { (argTask: TaskKey[Seq[String]]) ⇒
      // Here, we map the argument task `argTask`
      // and a normal setting `scalaVersion`
      (argTask) map { (args: Seq[String]) ⇒
        args foreach println
      }
    }
  )

  def createView(name: String) {
    Http(dispatch.url("http://www.scala-lang.org/") OK as.String)()
  }

  def addJobToView(view: String, job: String) {
    Http(dispatch.url(JenkinsBaseUrl + "/view/%s/addJobToView".format(view)) << Map("name" -> job) OK as.String)()
  }

  def getJobConfig(name: String) =
    Http(dispatch.url(JenkinsBaseUrl + "/job/%s/config.xml".format(name)) OK as.xml.Elem)()

  def getViewConfig(name: String) =
    Http(dispatch.url(JenkinsBaseUrl + "/view/%s/config.xml".format(name)) OK as.xml.Elem)()

  def updateJobConfig(jobName: String, config: Seq[scala.xml.Node]) =
    Http(dispatch.url(JenkinsBaseUrl + "/job/%s/config.xml".format(jobName)).POST.setBody(config.mkString) OK as.String)()

  def updateJobBranch(job: String, newBranch: String) {
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

  def getProjectViewName(view: String, project: String) = view + "_" + project

  def copyJob(orig: String, newJob: String) {
    val params = Map("name" -> newJob, "mode" -> "copy", "from" -> orig)
    Http(dispatch.url(JenkinsBaseUrl + "/createItem") << params)()
  }

  def buildJob(job: String) {
    Http(dispatch.url(JenkinsBaseUrl + "/job/%s/build".format(job)))()
  }

  def buildAllJobsInView(view: String) {
    getJobsInView(view).foreach { job ⇒ println(job); buildJob(job) }
  }

  def getJobsInView(view: String) = {
    val config = Http(dispatch.url(JenkinsBaseUrl + "/view/%s/config.xml".format(view)) OK as.xml.Elem)()
    val nodes = config \\ "jobNames" \\ "string"
    nodes.map(_.text)
  }

  def getConfig(url: String) = {
    Http(dispatch.url(url) OK as.xml.Elem)()
  }

  def getCurrentBranch = {
    import scala.sys.process._
    "git rev-parse --abbrev-ref HEAD".!!
  }

  def getCurrentTicket = """([0-9]+)""".r.findFirstMatchIn(getCurrentBranch).map(_ group 1).get

  def getAllProjects(baseDir: File): Seq[Project] = {
    val pattern = ".*\"(.*)\".*\"(.*)\".*".r
    IO.readLines(baseDir).filter(pattern.findFirstIn(_).isDefined).map(line ⇒ {
      val groups = pattern.findAllIn(line).matchData.next
      Project(groups.group(1), groups.group(2))
    })
  }

  def getSnapShotProjects(baseDir: File): Seq[Project] = {
    getAllProjects(baseDir).filter(project ⇒ project.version.endsWith("SNAPSHOT"))
  }

  def buildSnapshotsInView(view: String, baseDir: File) {
    val jobsToBuildInOrder = getSnapshotsInView(view: String, baseDir: File)
    jobsToBuildInOrder.foreach(buildJob)
  }

  def getSnapshotsInView(view: String, baseDir: File) = {
    val projectNames = getSnapShotProjects(baseDir).map(_.name)
    val jobs = getJobsInView(view)
    projectNames.flatMap(project ⇒ jobs.filter(_.endsWith(project)))
  }

  case class Project(name: String, version: String)

}
package co.movio.jenkins

import sbt._
import Keys._
import sbt.TaskKey
import dispatch._
import scala.xml.Document
import scala.sys.process._

object JenkinsPlugin extends Plugin {
  object Keys {
    val copyProject = TaskKey[Unit]("jenCopyProject")
    val jenCreateView = InputKey[Unit]("jenCreateView")
  }
  import Keys._

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

  def addToView(view: String, job: String) {

  }

  def getJobConfig(jobName: String) = {
    // get http://jenkins.movio.co/job/${jobName}/config.xml
  }

  def updateJobConfig(jobName: String, config: Document) = {
    // post http://jenkins.movio.co/job/${jobName}/config.xml
  }

  def copyJob(orig: String, newJob: String) {
    // http://jenkins.movio.co/createItem?name=NEWJOBNAME&mode=copy&from=FROMJOBNAME
  }

  def buildJobByBranch(job: String, branch: String) {

  }

  def getJobsInView(view: String) = {
    //     #echo "Getting Projects in View $1"
    //     body=`curl -s http://jenkins.movio.co/view/$1/config.xml`
    //     echo `echo "$body" | grep "string" | sed -e "s/[^>]*>\([^<]*\)/\1/g"`

  }

  def getCurrentBranch = "git rev-parse --abbrev-ref HEAD".!!

  def getCurrentTicket = 
    //    echo `echo $fullBranch | sed -e "s/\([0-9]\+\).*/\1/g"`
    """\\([0-9]\\+\\).*""".r.findFirstMatchIn(getCurrentBranch).map(_ group 1)
    
  

  def getAllProjects(baseDir: File): Seq[Project] = {
    val pattern = ".*\"(.*)\".*\"(.*)\"".r
    IO.readLines(baseDir).filter(pattern.findFirstIn(_).isDefined).map(line ⇒ {
      val groups = pattern.findAllIn(line).matchData.next
      Project(groups.group(1), groups.group(2))
    })
  }

  def getSnapShotProjects(baseDir: File): Seq[Project] = {
    getAllProjects(baseDir).filter(project ⇒ project.version.endsWith("SNAPSHOT"))
  }

  def getViewConfig(viewName: String) = {
    // http://jenkins.movio.co/view/${viewName}/config.xml
  }

  def updateViewConfig(viewName: String, config: Document) = {
    // post http://jenkins.movio.co/view/${viewName}/config.xml
  }

  case class Project(name: String, version: String)

}
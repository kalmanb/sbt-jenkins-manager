package com.kalmanb.sbt.extra

import scala.xml.Elem
import scala.xml.Node
import scala.xml.transform.RewriteRule

import com.kalmanb.sbt.JenkinsPlugin.Jenkins
import sbt.Keys._
import sbt._

object JenkinsGitPlugin {
  import com.kalmanb.sbt.JenkinsPlugin._
  import Git._

  val jenChangeJobBranch = InputKey[Unit]("jenkinsChangeJobBranch",
    "<jobName> <branch> change a jobs git branch setting")
  val jenChangeViewBranch = InputKey[Unit]("jenkinsChangeViewBranch",
    "<viewName> <branch> change all jobs in the view to a new git branch setting")
  val jenChangeJobsBranch = InputKey[Unit]("jenkinsChangeJobsBranch",
    "<regex> <branch> change all jobs that match a regex to a new git branch setting")

  lazy val jenkinsGitSettings = Seq(
    jenChangeJobBranch <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).updateJob(args.head, changeJobGitBranch(args(1)))),
    jenChangeViewBranch <<= jenkinsTask(2, (baseUrl, args) ⇒
      changeViewGitBranch(baseUrl, args.head, args(1))),
    jenChangeJobsBranch <<= jenkinsTask(2, (baseUrl, args) ⇒
      changeJobsGitBranch(baseUrl, args.head, args(1)))
  )

  object Git {
    def changeJobGitBranch(newBranch: String)(config: Seq[scala.xml.Node]): Seq[scala.xml.Node] = {
      val updated = new RewriteRule {
        override def transform(n: scala.xml.Node): Seq[scala.xml.Node] = n match {
          case Elem(prefix, "hudson.plugins.git.BranchSpec", attribs, scope, child @ _*) ⇒ Elem(prefix, "hudson.plugins.git.BranchSpec", attribs, scope, <name>{ newBranch }</name>: _*)
          case elem: Elem ⇒ elem copy (child = elem.child flatMap (this transform))
          case other ⇒ other
        }
      } transform config
      updated
    }

    def changeViewGitBranch(baseUrl: String, view: String, newBranch: String): Unit = {
      val jenkins = Jenkins(baseUrl)
      jenkins.getJobsInView(view).foreach(jenkins.updateJob(_, changeJobGitBranch(newBranch)))
    }

    def changeJobsGitBranch(baseUrl: String, regex: String, newBranch: String): Unit = {
      val jenkins = Jenkins(baseUrl)
      val pattern = new scala.util.matching.Regex(regex)
      jenkins.getAllJobs().filter(
        job ⇒ pattern findFirstIn job isDefined).foreach { job ⇒
          println("Changing branch to " + newBranch + " for job " + job + ".")
          jenkins.updateJob(job, changeJobGitBranch(newBranch))
        }
    }
  }

}

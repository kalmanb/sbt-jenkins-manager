package com.kalmanb.sbt


import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._
import sbt.complete.Parser

object JenkinsPlugin extends JenkinsPluginTrait
trait JenkinsPluginTrait extends Plugin {

  // Settings for Build.scala
  val jenkinsBaseUrl = SettingKey[String]("jenkinsBaseUrl", "The base URL for your Jenkins Server, eg http://jenkins.foo.com")

  // Tasks
  val jenCopyJob = InputKey[Unit]("jenCopyJob",
    "<scr> <dest> create a copy of an existing job")
  val jenBuildJob = InputKey[Unit]("jenBuildJob",
    "<jobName> start a build for a Job")
  val jenDeleteJob = InputKey[Unit]("jenDeleteJob",
    "<jobName> delete Job from Jenkins")
  val jenDeleteJobRegex = InputKey[Unit]("jenDeleteJobRegex",
    "<jobRegex> delete Job from Jenkins")
  val jenCreateView = InputKey[Unit]("jenCreateView",
    "<name> create a new view")
  val jenCopyView = InputKey[Unit]("jenCopyView",
    "<src> <dst> [prefix] creates a new view with name <dst> and duplicates all jobs in <src>. Prefix is for the new jobs, it's optional and defaults to <dst>")
  val jenAddJobToView = InputKey[Unit]("jenAddJobToView",
    "<jobName> <viewName> create a new view")
  val jenDeleteView = InputKey[Unit]("jenDeleteView",
    "<name> deletes the view, does NOT delete the jobs in the view")
  val jenDeleteViewAndJobs = InputKey[Unit]("jenDeleteViewAndJobs",
    "<name> deletes the view and deletes all the jobs in the view")
  val jenBuildAllJobsInView = InputKey[Unit]("jenBuildAllJobsInView",
    "<name> queues the build of all jobs")
  val jenSetWipeoutWorkspaceView = InputKey[Unit]("jenSetWipeoutWorkspaceView",
    "<view> <true|false> [ignore,projects] - changes the setting for wipeout workspace in the specified view")
  val jenChangeViewSbtActions = InputKey[Unit]("jenChangeViewSbtActions",
    "<view> -<job-exclusion> -<job-exclusion> <action> <action> ... - changes the the actions based on the job eg ;job1/clean;job1/publish. A param starting with '-' will be a job that is not changed")

  lazy val jenkinsSettings = Seq(
    jenCopyJob <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).copyJob(args.head, args(1))),
    jenBuildJob <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).buildJob(args.head)),
    jenDeleteJob <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).deleteJob(args.head)),
    jenDeleteJobRegex <<= jenkinsTask(1, (baseUrl, args) ⇒
      Jenkins(baseUrl).deleteJobRegex(args.head)),
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
      Jenkins(baseUrl).setWipeOutWorkspaceForView(args)),
    jenChangeViewSbtActions <<= jenkinsTask(2, (baseUrl, args) ⇒
      Jenkins(baseUrl).updateViewSbtActions(args.head, args.tail)))
 

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

}

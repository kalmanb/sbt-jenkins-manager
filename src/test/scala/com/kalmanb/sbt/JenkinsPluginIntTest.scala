package com.kalmanb.sbt

import java.io.File

import org.scalatest.FunSpec
import sbt.Project

class JenkinsPluginIntTest extends FunSpec {
  import Common._

  describe("Jenkins API") {
    it("should be able to create and remove a view") {
      val view = "testView"
      assert(doesViewExist(view) === false)
      TestServer.createView(view)
      assert(doesViewExist(view) === true)
      TestServer.deleteView(view)
      assert(doesViewExist(view) === false)
    }

    it("should be able to create and delete jobs") {
      val job = "testJob"
      assert(doesJobExist(job) === false)
      TestServer.createJob(job, gitProject)
      assert(doesJobExist(job) === true)
      TestServer.deleteJob(job)
      assert(doesJobExist(job) === false)
    }

    it("should be able to add jobs to a view, list them and delete the whole view") {
      val view = "view"
      val job1 = "job1"
      val job2 = "job2"
      TestServer.createView(view)
      TestServer.createJob(job1, gitProject)
      TestServer.createJob(job2, gitProject)
      TestServer.addJobToView(job1, view)
      TestServer.addJobToView(job2, view)
      assert(TestServer.getViewConfig(view).mkString contains "<string>job1</string>")
      assert(TestServer.getViewConfig(view).mkString contains "<string>job2</string>")
      TestServer.deleteViewAndJobs(view)
      assert(doesViewExist(view) === false)
      assert(doesJobExist(job1) === false)
      assert(doesJobExist(job2) === false)
    }

    //        def createJob = http://localhost:8080/createItem
  }
  //    it("should be able to get view jobs") {
  //      // Working
  //      println(TestServer.getJobsInView("view"))
  //    }
  // it("should set the git plugin to wipe workspaces") {
  //   // Working
  //   println(TestServer.setWipeOutWorkspaceForView(Seq("fg", "true")))
  // }
  //
  //    it("should copy jobs") {
  //      // Working
  //      TestServer.copyJob("src", "dst")
  //    }
  //
  //    it("add Job to view") {
  //      // Working
  //      TestServer.addJobToView("job", "view")
  //    }
  //
  //    it("update Job branch") {
  //      // Working
  //      TestServer.changeJobGitBranch("job", "branch")
  //    }
  //
  //    it("should build a job") {
  //      // Working
  //      TestServer.buildJob("job")
  //    }
  //
  //    it("should delete a job") {
  //      // Working
  //      TestServer.deleteJob("job")
  //    }
  //
  //    it("should build all Jobs in View") {
  //      // Working
  //      TestServer.buildAllJobsInView("view")
  //    }
  //
  //    it("should create new view") {
  //      // Working
  //      TestServer.createView("view")
  //    }
  //
  //    it("should delete a view") {
  //      // Working
  //      TestServer.deleteView("view")
  //    }
  //
  // it("should duplicate a view with a copy of all its jobs") {
  //      // Working
  //      TestServer.copyView("src", "dst")
  // }
  // it("should copy a view from one server to another") {
  //      // Working
  //      TestServer.copyViewToOtherServer("http://jenkins1", "http://jenkins2", "viewName")
  //    }
  //
  //    it("should delete a view and delete all jobs included in that view") {
  //      // Working
  //      TestServer.copyView("src", "dst")
  //      TestServer.deleteViewAndJobs("dst")
  //    }
  //
  //  }
  // it ("should change the branch for all matching jobs") {
  //   TestServer.changeJobsGitBranch("pattern", "master")
  // }

  def doesViewExist(view: String) =
    try {
      TestServer.getViewConfig(view)
      true
    } catch { case _: Exception ⇒ false }

  def doesJobExist(job: String) =
    try {
      TestServer.getJobConfig(job)
      true
    } catch { case _: Throwable ⇒ false }
}

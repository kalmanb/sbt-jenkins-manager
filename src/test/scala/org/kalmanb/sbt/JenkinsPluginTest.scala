package org.kalmanb.sbt

import java.io.File

import org.scalatest.FunSpec

import sbt.Project

class jenkinsTest extends FunSpec {

  val jenkins = JenkinsPlugin.Jenkins("http://jenkins.movio.co/")
  
  describe("jenkins API") {

    it("should be able to get view jobs") {
      // Working
      println(jenkins.getJobsInView("view"))
    }

    it("should copy jobs") {
      // Working
      jenkins.copyJob("src", "dst")
    }

    it("add Job to view") {
      // Working
      jenkins.addJobToView("job", "view")
    }

    it("update Job branch") {
      // Working
      jenkins.changeJobGitBranch("job", "branch")
    }

    it("should build a job") {
      // Working
      jenkins.buildJob("job")
    }

    it("should delete a job") {
      // Working
      jenkins.deleteJob("job")
    }

    it("should build all Jobs in View") {
      // Working
      jenkins.buildAllJobsInView("view")
    }

    it("should create new view") {
      // Working
      jenkins.createView("view")
    }

    it("should delete a view") {
      // Working
      jenkins.deleteView("view")
    }

    it("should duplicate a view with a copy of all its jobs") {
      // Working
      jenkins.copyView("src", "dst")
    }

    it("should delete a view and delete all jobs included in that view") {
      // Working
      jenkins.copyView("src", "dst")
      jenkins.deleteViewAndJobs("dst")
    }

  }

}
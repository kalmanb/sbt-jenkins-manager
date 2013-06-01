package org.kalmanb.sbt

import java.io.File

import org.scalatest.FunSpec

import sbt.Project

class jenkinsTest extends FunSpec {

  // You'll need a running version of Jenkins for these tests
  // It will need the Git Plugin installed
  val jenkins = JenkinsPlugin.Jenkins("http://localhost:8080/")

  describe("jenkins API") {
    it("should be able to create and remove a view") {
      val view = "testView"
      assert(doesViewExist(view) === false)
      jenkins.createView(view)
      assert(doesViewExist(view) === true)
      jenkins.deleteView(view)
      assert(doesViewExist(view) === false)
    }

    it("should be able to create and delete jobs") {
      val job = "testJob"
      assert(doesJobExist(job) === false)
      jenkins.createJob(job, Examples.gitProject)
      assert(doesJobExist(job) === true)
      jenkins.deleteJob(job)
      assert(doesJobExist(job) === false)
    }

    it("should be able to add jobs to a view, list them and delete the whole view") {
      val view = "view"
      val job1 = "job1"
      val job2 = "job2"
      jenkins.createView(view)
      jenkins.createJob(job1, Examples.gitProject)
      jenkins.createJob(job2, Examples.gitProject)
      jenkins.addJobToView(job1, view)
      jenkins.addJobToView(job2, view)
      assert(jenkins.getViewConfig(view).mkString contains "<string>job1</string>")
      assert(jenkins.getViewConfig(view).mkString contains "<string>job2</string>")
      jenkins.deleteViewAndJobs(view)
      assert(doesViewExist(view) === false)
      assert(doesJobExist(job1) === false)
      assert(doesJobExist(job2) === false)
    }

    //        def createJob = http://localhost:8080/createItem
  }
  //    it("should be able to get view jobs") {
  //      // Working
  //      println(jenkins.getJobsInView("view"))
  //    }
  //
  //    it("should copy jobs") {
  //      // Working
  //      jenkins.copyJob("src", "dst")
  //    }
  //
  //    it("add Job to view") {
  //      // Working
  //      jenkins.addJobToView("job", "view")
  //    }
  //
  //    it("update Job branch") {
  //      // Working
  //      jenkins.changeJobGitBranch("job", "branch")
  //    }
  //
  //    it("should build a job") {
  //      // Working
  //      jenkins.buildJob("job")
  //    }
  //
  //    it("should delete a job") {
  //      // Working
  //      jenkins.deleteJob("job")
  //    }
  //
  //    it("should build all Jobs in View") {
  //      // Working
  //      jenkins.buildAllJobsInView("view")
  //    }
  //
  //    it("should create new view") {
  //      // Working
  //      jenkins.createView("view")
  //    }
  //
  //    it("should delete a view") {
  //      // Working
  //      jenkins.deleteView("view")
  //    }
  //
  //    it("should duplicate a view with a copy of all its jobs") {
  //      // Working
  //      jenkins.copyView("src", "dst")
  //    }
  //
  //    it("should delete a view and delete all jobs included in that view") {
  //      // Working
  //      jenkins.copyView("src", "dst")
  //      jenkins.deleteViewAndJobs("dst")
  //    }
  //
  //  }
  def doesViewExist(view: String) =
    try {
      jenkins.getViewConfig(view)
      true
    } catch { case _: Exception ⇒ false }

  def doesJobExist(job: String) =
    try {
      jenkins.getJobConfig(job)
      true
    } catch { case _: Throwable ⇒ false }
}

object Examples {
  val gitProject =
    <project>
      <actions/>
      <description/>
      <keepDependencies>false</keepDependencies>
      <properties/>
      <scm class="hudson.plugins.git.GitSCM" plugin="git@1.4.0">
        <configVersion>2</configVersion>
        <userRemoteConfigs>
          <hudson.plugins.git.UserRemoteConfig>
            <name/>
            <refspec/>
            <url>/tmp</url>
          </hudson.plugins.git.UserRemoteConfig>
        </userRemoteConfigs>
        <branches>
          <hudson.plugins.git.BranchSpec>
            <name>**</name>
          </hudson.plugins.git.BranchSpec>
        </branches>
        <disableSubmodules>false</disableSubmodules>
        <recursiveSubmodules>false</recursiveSubmodules>
        <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
        <authorOrCommitter>false</authorOrCommitter>
        <clean>false</clean>
        <wipeOutWorkspace>false</wipeOutWorkspace>
        <pruneBranches>false</pruneBranches>
        <remotePoll>false</remotePoll>
        <ignoreNotifyCommit>false</ignoreNotifyCommit>
        <useShallowClone>false</useShallowClone>
        <buildChooser class="hudson.plugins.git.util.DefaultBuildChooser"/>
        <gitTool>Default</gitTool>
        <submoduleCfg class="list"/>
        <relativeTargetDir/>
        <reference/>
        <excludedRegions/>
        <excludedUsers/>
        <gitConfigName/>
        <gitConfigEmail/>
        <skipTag>false</skipTag>
        <includedRegions/>
        <scmName/>
      </scm>
      <canRoam>true</canRoam>
      <disabled>false</disabled>
      <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
      <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
      <triggers class="vector"/>
      <concurrentBuild>false</concurrentBuild>
      <builders/>
      <publishers/>
      <buildWrappers/>
    </project>

}
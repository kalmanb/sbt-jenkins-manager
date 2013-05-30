package co.movio.sbt

import java.io.File

import org.scalatest.FunSpec

import JenkinsPlugin._

class JenkinsPluginTest extends FunSpec {

  val testVersionsFile = new File("src/test/resources/Versions.scala")

  describe("versions") {
    it("should get all projects") {
      val projects = JenkinsPlugin.getAllProjects(testVersionsFile)
      val expected = Set(
        Project("logging", "1.0.2"),
        Project("testtools", "1.0.1"),
        Project("testme1", "1.0.1-SNAPSHOT"),
        Project("testme2", "6.1.0-SNAPSHOT"),
        Project("domain", "6.1.0"))
      assert(projects.toSet === expected)
    }

    it("should get all snapshot projects") {
      val projects = JenkinsPlugin.getSnapShotProjects(testVersionsFile)
      val expected = Set(
        Project("testme1", "1.0.1-SNAPSHOT"),
        Project("testme2", "6.1.0-SNAPSHOT"))
      assert(projects.toSet === expected)
    }

    it("should get project/view name") {
      assert(JenkinsPlugin.getProjectViewName("view", "project") === "view_project")
    }
  }

  describe("git") {
    it("should get the current branch") {
      println(JenkinsPlugin.getCurrentBranch)
    }
    it("should get current ticket") {
      println(JenkinsPlugin.getCurrentTicket)
    }
  }

  describe("jenkins") {
    it("should be able to get view jobs") {
      // Working
      //       println(JenkinsPlugin.getJobsInView("Kal"))
    }

    it("should copy jobs") {
      // Working
      //JenkinsPlugin.copyJob("testme", "newJob")
    }

    it("add Job to view") {
      // Working
      // JenkinsPlugin.addJobToView("Release", "testme")
    }

    it("update Job branch") {
      // Workin
      // JenkinsPlugin.updateJobBranch("testme", "kal")
    }

    it("should build a job") {
      // Working
      //       JenkinsPlugin.buildJob("ATM_logging_continuous-kal")
    }

    it("should build all Jobs in View") {
      // Working
      //      JenkinsPlugin.buildAllJobsInView("Kal")
    }

    it("should build all current snapshots in view") {
      // Working
      //      JenkinsPlugin.buildSnapshotsInView("Kal", testVersionsFile)
    }

    it("should calculate the correct snapshots in view") {
      new JenkinsPluginTrait {
        override def getSnapShotProjects(baseDir: File) = Seq(
          Project("one", "1-SNAPSHOT"),
          Project("two", "1-SNAPSHOT"),
          Project("four", "1-SNAPSHOT"))
        override def getJobsInView(view: String) = List("aa_five", "aa_two", "aa_one")
     
        assert(List("aa_one", "aa_two") === this.getSnapshotsInView("", testVersionsFile))
      }
    }

    it("should duplicate a view for new branch") {

    }

  }

}
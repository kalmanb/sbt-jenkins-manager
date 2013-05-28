package co.movio.jenkins

import org.scalatest.FunSpec
import java.io.File
import JenkinsPlugin._

class JenkinsPluginTest extends FunSpec {

  describe("jenkins REST API") {

    it("should ") {

    }

  }

  describe("versions") {
    it("should get all projects") {
      val projects = JenkinsPlugin.getAllProjects(new File("src/test/resources/Versions.scala"))
      val expected = Set(
        Project("logging", "1.0.2"),
        Project("testtools", "1.0.1"),
        Project("akkatools", "1.0.1-SNAPSHOT"),
        Project("core", "6.1.0-SNAPSHOT"),
        Project("domain", "6.1.0"))
      assert(projects.toSet === expected)
    }
   
    it("should get all snapshot projects") {
      val projects = JenkinsPlugin.getSnapShotProjects(new File("src/test/resources/Versions.scala"))
      val expected = Set(
        Project("akkatools", "1.0.1-SNAPSHOT"),
        Project("core", "6.1.0-SNAPSHOT"))
      assert(projects.toSet === expected)
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

}
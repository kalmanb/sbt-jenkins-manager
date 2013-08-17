package com.kalmanb.sbt.extra

import org.scalatest.FunSpec

class JenkinsGitPluginTest extends FunSpec {
  import JenkinsGitPlugin.Git._

  val example =
    <project>
      <description/>
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
      </scm>
    </project>

  describe("change job git branch") {
    it("should be update name") {
      val result = changeJobGitBranch("new-test")(example)
      assert((result \ "_" \ "_" \ "hudson.plugins.git.BranchSpec" \ "name").text === "new-test")
    }
  }
}

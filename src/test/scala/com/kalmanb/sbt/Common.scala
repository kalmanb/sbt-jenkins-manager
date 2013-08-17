package com.kalmanb.sbt

object Common {
  // To run tests against the a server you'll need one running
  val TestServer = JenkinsPlugin.Jenkins("http://localhost:8080/")
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
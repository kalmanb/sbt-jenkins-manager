# sbt-jenkins-manager

Drive Jenkins from within SBT

Building


    git clone git@github.com:kalmanb/sbt-jenkins-manager.git
    cd sbt-jenkins-manager
    sbt
    publish-local

You may need:

    ++ 2.9.2


Include in a project:
In your `project/plugins.sbt` add the following line:

    addSbtPlugin("org.kalmanb" % "sbt-jenkins-manager" % "0.3.0")

Add your jenkins url to your project settings:

    import org.kalmanb.sbt.JenkinsPlugin._

    lazy val name = Project(
       ...
       settings = jenkinsSettings ++ Seq(
         ...
         jenkinsBaseUrl := "http://jenkins.myhost.com/"
      )
    )

Usage:
 sbt

    jenkinsCopyJob <scr> <dest> create a copy of an existing job
    jenkinsBuildJob <job-name> start a build for a Job
    jenkinsDeleteJob <job-name> delete Job from Jenkins
    jenkinsDeleteJobRegex <job-regex> delete Job from Jenkins
    jenkinsChangeJobBranch <job-name> <branch> change a jobs git branch setting
    jenkinsChangeViewBranch <view-name> <branch> change all jobs in the view to a new git branch setting
    jenkinsChangeJobsBranch <regex> <branch> change all jobs that match a regex to a new git branch setting
    jenkinsCreateView <name> create a new view
    jenkinsCopyView <src> <dst> [prefix] creates a new view with name <dst> and duplicates all jobs in <src>. Prefix is for the new jobs, it's optional and defaults to <dst>
    jenkinsAddJobToView <job-name> <view-name> create a new view
    jenkinsDeleteView <name> deletes the view, does NOT delete the jobs in the view
    jenkinsDeleteViewAndJobs <name> deletes the view and deletes all the jobs in the view
    jenkinsBuild-allJobsInView <name> queues the build of all jobs
    jenkinsSetWipeoutWorkspaceView <view> <true|false> [ignore,projects] - changes the setting for wipeout workspace in the specified view
    jenkinsChangeViewThrottleCats <view> <cat1,cat2,cat3> [ignore,projects] -changes the setting for wipeout workspace in the specified view

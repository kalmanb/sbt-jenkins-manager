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

    addSbtPlugin("com.kalmanb" % "sbt-jenkins-manager" % "0.3.0")

Add your jenkins url to your project settings:

    import com.kalmanb.sbt.JenkinsPlugin._

    lazy val name = Project(
       ...
       settings = jenkinsSettings ++ Seq(
         ...
         jenkinsBaseUrl := "http://jenkins.myhost.com/"
      )
    )

Usage:
 sbt

    jenCopyJob <scr> <dest> create a copy of an existing job
    jenBuildJob <job-name> start a build for a Job
    jenDeleteJob <job-name> delete Job from Jenkins
    jenDeleteJobRegex <job-regex> delete Job from Jenkins
    jenChangeJobBranch <job-name> <branch> change a jobs git branch setting
    jenChangeViewBranch <view-name> <branch> change all jobs in the view to a new git branch setting
    jenChangeJobsBranch <regex> <branch> change all jobs that match a regex to a new git branch setting
    jenCreateView <name> create a new view
    jenCopyView <src> <dst> [prefix] creates a new view with name <dst> and duplicates all jobs in <src>. Prefix is for the new jobs, it's optional and defaults to <dst>
    jenAddJobToView <job-name> <view-name> create a new view
    jenDeleteView <name> deletes the view, does NOT delete the jobs in the view
    jenDeleteViewAndJobs <name> deletes the view and deletes all the jobs in the view
    jenBuild-allJobsInView <name> queues the build of all jobs
    jenSetWipeoutWorkspaceView <view> <true|false> [ignore,projects] - changes the setting for wipeout workspace in the specified view
    jenChangeViewThrottleCats <view> <cat1,cat2,cat3> [ignore,projects] -changes the setting for wipeout workspace in the specified view

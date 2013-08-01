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
         jenkinsBaseUrl := "http://jenkins.hostname.com/"
      )
    )

Usage:
 sbt

    jenkins-copy-job <scr> <dest> create a copy of an existing job
    jenkins-build-job <job-name> start a build for a Job
    jenkins-delete-job <job-name> delete Job from Jenkins
    jenkins-change-job-branch <job-name> <branch> change a jobs git branch setting
    jenkins-change-view-branch <view-name> <branch> change all jobs in the view to a new git branch setting
    jenkins-change-jobs-branch <regex> <branch> change all jobs that match a regex to a new git branch setting
    jenkins-create-view <name> create a new view
    jenkins-copy-view <src> <dst> [prefix] creates a new view with name <dst> and duplicates all jobs in <src>. Prefix is for the new jobs, it's optional and defaults to <dst>
    jenkins-add-job-to-view <job-name> <view-name> create a new view
    jenkins-delete-view <name> deletes the view, does NOT delete the jobs in the view
    jenkins-delete-view-and-jobs <name> deletes the view and deletes all the jobs in the view
    jenkins-build-all-jobs-in-view <name> queues the build of all jobs
    jenkins-set-wipeout-workspace-view <view> <true|false> [ignore,projects] - changes the setting for wipeout workspace in the specified view
    jenkins-change-view-throttle-cats <view> <cat1,cat2,cat3> [ignore,projects] -changes the setting for wipeout workspace in the specified view

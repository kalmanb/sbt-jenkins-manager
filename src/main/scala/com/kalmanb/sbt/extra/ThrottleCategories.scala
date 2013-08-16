package com.kalmanb.sbt.extra

import scala.xml.Elem
import scala.xml.Node
import scala.xml.transform.RewriteRule

import com.kalmanb.sbt.JenkinsPlugin.Jenkins
import sbt.Keys._
import sbt._

object ThrottleCategoriesPlugin extends Plugin {
  import com.kalmanb.sbt.JenkinsPlugin._
  import ThrottleCategories._

  val jenChangeThrottleCategories = InputKey[Unit]("jenkinsChangeViewThrottleCats", 
    "<view> <cat1,cat2,cat3> [ignore,projects] -changes the setting for wipeout workspace in the specified view")

  lazy val jenkinsSettings = Seq(               
    jenChangeThrottleCategories <<= jenkinsTask(2, (baseUrl, args) =>
        Jenkins(baseUrl).updateJob(args.head, changeThrottleCategoriesJob(args))
        ),
      jenChangeThrottleCategories <<= jenkinsTask(2, (baseUrl, args) =>
          changeThrottleCategoriesView(baseUrl, args)
          )
        )

  object ThrottleCategories {
    def changeThrottleCategoriesJob(categories: Seq[String])(config: Seq[Node]): Seq[Node] = {
      val settings =
        <wrapper>
          <maxConcurrentPerNode>0</maxConcurrentPerNode>
          <maxConcurrentTotal>0</maxConcurrentTotal>
          <categories>
          {categories.map(cat => <string>{cat}</string>)}
        </categories>
        <throttleEnabled>true</throttleEnabled>
        <throttleOption>category</throttleOption>
        <configVersion>1</configVersion>
      </wrapper>
    val updated = new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case Elem(prefix, "hudson.plugins.throttleconcurrents.ThrottleJobProperty", attribs, scope, child @ _*) ⇒
        Elem(prefix, "hudson.plugins.throttleconcurrents.ThrottleJobProperty", attribs, scope, settings \ "_":_*)
        case elem: Elem ⇒ elem copy (child = elem.child flatMap (this transform))
        case other ⇒ other
      }
    } transform config
    updated
    }

    def changeThrottleCategoriesView(baseUrl: String, args: Seq[String]): Unit = {
      val view = args.head
      val categoryList = args(1)
      val ignoreList = if(args.size > 2) Some(args(2)) else None
      val ignoredProjects = ignoreList.getOrElse("").split(",").map(_.trim)
      val categories = categoryList.split(",").map(_.trim)
      val jenkins = Jenkins(baseUrl)
      jenkins.getJobsInView(view).diff(ignoredProjects).foreach(
        jenkins.updateJob(_, changeThrottleCategoriesJob(categories))
      )
    }
  }

}

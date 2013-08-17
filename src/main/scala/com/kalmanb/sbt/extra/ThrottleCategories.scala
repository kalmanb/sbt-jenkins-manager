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

  lazy val throttleCategoriesSettings = Seq(
    jenChangeThrottleCategories <<= jenkinsTask(2, (baseUrl, args) ⇒
      changeThrottleCategoriesView(baseUrl, args)
    )
  )

  object ThrottleCategories {
    def changeThrottleCategoriesJob(categories: Seq[String])(config: Seq[Node]): Seq[Node] = {

      def createConfig(categories: Seq[String], config: Seq[Node]): Seq[Node] = {
        val newConfig =
          <hudson.plugins.throttleconcurrents.ThrottleJobProperty>
            <maxConcurrentPerNode>0</maxConcurrentPerNode>
            <maxConcurrentTotal>0</maxConcurrentTotal>
            <categories>
              { categories.map(cat ⇒ <string>{ cat }</string>) }
            </categories>
            <throttleEnabled>true</throttleEnabled>
            <throttleOption>category</throttleOption>
            <configVersion>1</configVersion>
          </hudson.plugins.throttleconcurrents.ThrottleJobProperty>

        config match {
          case Elem(prefix, label, attribs, scope, child @ _*) ⇒
            Elem(prefix, label, attribs, scope, child ++ newConfig: _*)
        }

      }
      def updateConfig(categories: Seq[String], config: Seq[Node]): Seq[Node] = {
        val updated = new RewriteRule {
          override def transform(n: Node): Seq[Node] = n match {
            case Elem(prefix, "hudson.plugins.throttleconcurrents.ThrottleJobProperty", attribs, scope, child @ _*) ⇒ {
              <hudson.plugins.throttleconcurrents.ThrottleJobProperty>
                { child.filter(_.label != "categories") }
                <categories> { categories.map(cat ⇒ <string>{ cat }</string>) } </categories>
              </hudson.plugins.throttleconcurrents.ThrottleJobProperty>
            }
            case elem: Elem ⇒ elem copy (child = elem.child flatMap (this transform))
            case other      ⇒ other
          }
        } transform config
        updated
      }

      if ((config \ "hudson.plugins.throttleconcurrents.ThrottleJobProperty").isEmpty) {
        createConfig(categories, config)
      } else {
        updateConfig(categories, config)
      }
    }

    def changeThrottleCategoriesView(baseUrl: String, args: Seq[String]): Unit = {
      val view = args.head
      val categoryList = args(1)
      val ignoreList = if (args.size > 2) Some(args(2)) else None
      val ignoredProjects = ignoreList.getOrElse("").split(",").map(_.trim)
      val categories = categoryList.split(",").map(_.trim)
      val jenkins = Jenkins(baseUrl)
      jenkins.getJobsInView(view).diff(ignoredProjects).foreach(
        jenkins.updateJob(_, changeThrottleCategoriesJob(categories))
      )
    }
  }

}

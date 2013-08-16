package com.kalmanb.sbt.extra

import org.scalatest.FunSpec
import scala.xml.Node

class ThrottleCategoriesTest extends FunSpec {
  import ThrottleCategoriesPlugin.ThrottleCategories._

  val throttleConfig = 
      <hudson.plugins.throttleconcurrents.ThrottleJobProperty>
        <maxConcurrentPerNode>0</maxConcurrentPerNode>
        <maxConcurrentTotal>2</maxConcurrentTotal>
        <categories>
          <string>start</string>
        </categories>
        <throttleEnabled>true</throttleEnabled>
        <throttleOption>category</throttleOption>
        <configVersion>1</configVersion>
      </hudson.plugins.throttleconcurrents.ThrottleJobProperty>

  def project(config: Seq[Node]) =
    <project>
      <description/>
      {config}
    </project>


  describe("Change throttle categories") {
    it("should update existing categories ") {
      val result = changeThrottleCategoriesJob(List("one", "two"))(project(throttleConfig))
      val newCategories = result \ "hudson.plugins.throttleconcurrents.ThrottleJobProperty" \ "categories" \ "string"
      assert(newCategories.text === "onetwo")
    }

    it("should not change the existing configuration") {
      val result = changeThrottleCategoriesJob(List("one", "two"))(project(throttleConfig))
      assert((result \ "_" \ "maxConcurrentTotal").text === "2")
    }

    it("create category if not present") {
      val result = changeThrottleCategoriesJob(List("start"))(project(List.empty))
      val newCategories = result \ "hudson.plugins.throttleconcurrents.ThrottleJobProperty" \ "categories" \ "string"
      assert(newCategories.text === "start")
    }
  }
}

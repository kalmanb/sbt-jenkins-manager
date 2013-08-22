package com.kalmanb.sbt

import dispatch._
import scala.annotation.tailrec

object SmartBuilder {
  def apply(baseUrl: String) = new SmartBuilder(baseUrl)
}
class SmartBuilder(baseUrl: String) extends Jenkins(baseUrl) {

  case class Build(number: Int, url: String)

  def getNextBuildNumber(job: String) =
    (getJobInfo(job) \ "nextBuildNumber").text.toInt

  def getLastBuild(job: String): Option[Build] = {
    val build = (getJobInfo(job) \ "build")
    if (build nonEmpty)
      Some(Build((build.head \ "number").text.toInt, (build.head \ "url").text))
    else None
  }

  def getJobStatus(job: String, buildNumber: Int): String = {
    val xml = Http(url(baseUrl + "/job/%s/%d/api/xml".format(job, buildNumber)) OK as.xml.Elem)()
    (xml \ "result").text
  }

  def waitForJobToAppear(job: String, nextBuildNumber: Int): Either[String, Build] = {
    // Usually takes about 10 seconds
    val NewJobWaitSeconds = 30
    val startTime = System.currentTimeMillis

    @tailrec
    def wait: Either[String, Build] = {
      if (System.currentTimeMillis > startTime + (NewJobWaitSeconds * 1000))
        Left("Timed out waiting for job to be created")
      else {
        print(".")
        Thread sleep 500
        val build = getLastBuild(job)
        build match {
          case Some(b) ⇒
            if (b.number == nextBuildNumber)
              Right(b)
            else
              wait
          case None ⇒ wait
        }
      }
    }
    wait
  }

  def waitForJobToComplete(job: String, buildNumber: Int): Either[String, String] = {
    val JobCompletionTimeout = 30 * 60 * 1000 // 30 mins
    val startTime = System.currentTimeMillis

    @tailrec
    def wait: Either[String, String] = {
      if (System.currentTimeMillis > startTime + (JobCompletionTimeout * 1000))
        Left("Timed out waiting for Job %s, build: %d to complete".format(job, buildNumber))
      else {
        print(".")
        Thread sleep 2 * 1000
        val build = getJobStatus(job, buildNumber)
        if (build equalsIgnoreCase "success") {
          println("\nJob: %s, build: %d completed".format(job, buildNumber))
          Right(job)
        } else if (build equalsIgnoreCase "failure")
          Left("ERROR: Job %s , build: %d FAILED - stopping".format(job, buildNumber))
        else
          wait
      }
    }
    println("\nWaiting for Job: %s, build: %d to complete".format(job, buildNumber))
    wait
  }

  def buildJobsInSequence(jobs: Seq[String]): Unit = {

    def completeJob(job: String): Either[String, String] = {
      val next = getNextBuildNumber(job)
      val build = buildJob(job)
      println("Building Job: %s, build: %d".format(job, next))

      for {
        _ ← waitForJobToAppear(job, next).right
        result ← waitForJobToComplete(job, next).right
      } yield result
    }

    @tailrec
    def work(remaining: Seq[String]): Unit = {
      if (remaining.isEmpty)
        println("All Jobs Complete")
      else {
        val jobResult = completeJob(remaining.head)
        jobResult match {
          case Left(e)  ⇒ println("ERROR: did not complete " + e)
          case Right(j) ⇒ work(remaining.tail)
        }
      }
    }
    work(jobs)
  }

}


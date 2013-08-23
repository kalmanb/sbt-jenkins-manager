package com.kalmanb.sbt

import scala.annotation.tailrec
import scala.collection.JavaConversions._

import dispatch._

object SmartBuilder {
  def apply(baseUrl: String) = new SmartBuilder(baseUrl)
}
class SmartBuilder(baseUrl: String) extends Jenkins(baseUrl) {

  def waitForJobToProcess(queueUrl: String): Either[String, String] = {
    val NewJobWaitMinutes = 30
    val startTime = System.currentTimeMillis

    @tailrec
    def wait: Either[String, String] = {
      if (System.currentTimeMillis > startTime + (NewJobWaitMinutes * 60 * 1000))
        Left("Timed out waiting for job to be processed")
      else {
        print(".")
        Thread sleep 5 * 1000
        val build = Http(url(queueUrl + "/api/xml") OK as.xml.Elem)()
        val executable = build \ "executable"
        if (executable.nonEmpty)
          Right((executable \ "url").text)
        else
          wait
      }
    }
    wait
  }

  def waitForJobToComplete(jobUrl: String): Either[String, String] = {
    val JobCompletionTimeout = 30 * 60 * 1000 // 30 mins
    val startTime = System.currentTimeMillis

    @tailrec
    def wait: Either[String, String] = {
      if (System.currentTimeMillis > startTime + (JobCompletionTimeout * 1000))
        Left("Timed out waiting for Job %s to complete".format(jobUrl))
      else {
        val xml = Http(url(jobUrl + "/api/xml") OK as.xml.Elem)()
        val buildResult = (xml \ "result").text

        buildResult.toLowerCase match {
          case "success" ⇒
            println("\nJob: %s, completed".format(jobUrl))
            Right(jobUrl)
          case "aborted" ⇒
            Left("ERROR: Job %s ABORTED - stopping".format(jobUrl))
          case "failure" ⇒
            Left("ERROR: Job %s FAILED - stopping".format(jobUrl))
          case _ ⇒
            print(".")
            Thread sleep 5 * 1000
            wait
        }
      }
    }
    println("\nWaiting for Job: %s to complete".format(jobUrl))
    wait
  }

  def buildJobAndWait(job: String): Either[String, String] = {
    val build = buildJob(job)
    val queued = build.getHeaders("Location")(0)
    for {
      jobUrl ← waitForJobToProcess(queued).right
      result ← waitForJobToComplete(jobUrl).right
    } yield result
  }

  def buildJobsInSequence(jobs: Seq[String]): Either[String, String] = {

    @tailrec
    def work(remaining: Seq[String]): Either[String, String] = {
      if (remaining.isEmpty) {
        println("All Jobs Complete")
        Right("")
      } else {
        val jobResult = buildJobAndWait(remaining.head)
        jobResult match {
          case Left(e) ⇒
            println("ERROR: did not complete " + e)
            Left("")
          case Right(j) ⇒ work(remaining.tail)
        }
      }
    }
    work(jobs)
  }
}


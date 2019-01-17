#!/usr/bin/env scala

import java.io.{File, InputStream}
import scala.collection.Map
import scala.collection.mutable.ListBuffer
import scala.io.Source
import sys.process.{Process, ProcessLogger}

val gitRepo = new File(Process("git rev-parse --show-toplevel").!!.trim)
val exports = Map.empty

def printAndAppend(printFunc: String => Unit, buffer: ListBuffer[String]): String => Unit =
  (line: String) => {
    printFunc(line)
    buffer += line
  }

def run(cmd: String, cwd: File) = {
  println(s"+ $cmd in $cwd")
  var stdoutLines = ListBuffer.empty[String]
  var stderrLines = ListBuffer.empty[String]
  val processLogger = ProcessLogger(
    printAndAppend(println, stdoutLines),
    printAndAppend(Console.err.println, stderrLines)
  )
  val result = Process(cmd, cwd, exports.toSeq: _*).run(processLogger).exitValue
  if (result != 0) {
    println(s"$cmd failed; exit code: $result")
    sys.exit(result)
  }
  (result, stdoutLines, stderrLines)
}

implicit class runInGitRepo(cmd: String) {
  def ! = run(cmd, gitRepo)._1
  def !! = run(cmd, gitRepo)._2
}

val branch = "git rev-parse --abbrev-ref HEAD".!!.head
"git checkout master".!
"git pull".!
s"git checkout $branch".!
"git merge master".!

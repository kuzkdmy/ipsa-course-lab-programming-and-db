package ua.kpi.ipsa

import doobie.Update0
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.slf4j.{Logger, LoggerFactory}
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.quick.quickRequest
import sttp.client3.{Empty, RequestT, SttpBackend}
import zio.duration.durationInt
import zio.interop.catz._
import zio.test.{DefaultRunnableSpec, TestAspect}
import zio.{Has, RManaged, Schedule, Task, TaskManaged, ZIO, ZManaged}

import scala.util.Try

trait BaseFunTest extends DefaultRunnableSpec {

  type Backend = SttpBackend[Task, ZioStreams with WebSockets]
  val logger: Logger                  = LoggerFactory.getLogger(this.getClass)
  val c: RequestT[Empty, String, Any] = quickRequest

  // zio tests, even from sbt don't await when previous test close all resources, so start and failed to bind address
  private val retryScheduler = Schedule.exponential(1.seconds) && Schedule.recurs(5)
  override def aspects: List[TestAspect[Nothing, _root_.zio.test.environment.TestEnvironment, Nothing, Any]] =
    List(TestAspect.timeoutWarning(10.seconds), TestAspect.timeout(20.seconds), TestAspect.sequential, TestAspect.retry(retryScheduler))

  def evalDb(fileName: String): RManaged[Has[Transactor[Task]], Int] = {
    for {
      rawSql <- loadContent(fileName)
      tx     <- ZIO.service[Transactor[Task]].toManaged_
      res    <- Update0(rawSql, None).run.transact(tx).toManaged_
    } yield res
  }

  def loadContent(fileName: String): TaskManaged[String] = {
    ZManaged
      .fromAutoCloseable(ZIO(Thread.currentThread().getContextClassLoader.getResourceAsStream(fileName)))
      .flatMap(source => ZManaged.fromTry(Try(scala.io.Source.fromInputStream(source).mkString)))
      .tapError(err => {
        logger.error(s"Failed to load file : $fileName", err)
        ZManaged.fail(err)
      })
  }
}

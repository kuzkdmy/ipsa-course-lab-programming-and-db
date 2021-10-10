package ua.kpi.ipsa.trace

import cats.Show
import org.slf4j.{Logger, MDC}
import zio.UIO

import scala.jdk.CollectionConverters._

case class Ctx(requestId: String)
object Ctx {
  implicit val show: Show[Ctx] = (t: Ctx) => s"""{"x-request-id": ${t.requestId}"""
}

// I hesitate a lot about usage of
// zio logger <- don't see how to force use correct classname in all the places,
// for zio logger I also seen that log.locally with mdc not propagated, may be now this works, anyway don't want to write this setMdc all the time
// also think about Ctx as part of zio env, this is not so easy to be done correct and spoil all Task[T] to be at least RIO[Has[Logger], T]
// variant with Scala 3 contextual functions is not bad, but Scala 3 support from Intellij is horrible now
// so stay with 2 implicit
// anyway any choice is dramatically invasive and all application code start depend on it, as for me now implicit wins
// zio 2 did a lot of changes related to logs, need to check what it will be
object log {
  private def ctxLog(logFn: => Unit)(implicit ctx: Ctx): Unit = {
    try {
      MDC.setContextMap(Map("x-request-id" -> ctx.requestId).asJava)
      logFn
    } finally {
      MDC.clear()
    }
  }

  def error(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]               = UIO(ctxLog(l.error(msg)))
  def warn(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]                = UIO(ctxLog(l.warn(msg)))
  def info(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]                = UIO(ctxLog(l.info(msg)))
  def debug(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]               = UIO(ctxLog(l.debug(msg)))
  def trace(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]               = UIO(ctxLog(l.trace(msg)))
  def error(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit] = UIO(ctxLog(l.error(msg, t)))
  def warn(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit]  = UIO(ctxLog(l.warn(msg, t)))
  def info(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit]  = UIO(ctxLog(l.info(msg, t)))
  def debug(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit] = UIO(ctxLog(l.debug(msg, t)))
  def trace(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit] = UIO(ctxLog(l.trace(msg, t)))
}

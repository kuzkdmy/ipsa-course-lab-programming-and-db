package ua.kpi.ipsa.trace

import org.slf4j.Logger
import zio.UIO

case class Ctx(requestId: String)

// I hesitate a lot about usage of
// zio logger <- don't see how to force use correct classname in all the places,
// for zio logger I also seen that log.locally with mdc not propagated, may be now this works, anyway don't want to write this setMdc all the time
// also think about Ctx as part of zio env, this is not so easy to be done correct and spoil all Task[T] to be at least RIO[Has[Logger], T]
// variant with Scala 3 contextual functions is not bad, but Scala 3 support from Intellij is horrible now
// so stay with 2 implicit
// anyway any choice is dramatically invasive and all application code start depend on it, as for me now implicit wins
// zio 2 did a lot of changes related to logs, need to check what it will be
object log {
  private def ctxMsg(ctx: Ctx, m: => String): String = s"x-request-id:[${ctx.requestId}] - $m"

  def error(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]               = UIO(l.error(ctxMsg(ctx, msg)))
  def warn(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]                = UIO(l.warn(ctxMsg(ctx, msg)))
  def info(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]                = UIO(l.info(ctxMsg(ctx, msg)))
  def debug(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]               = UIO(l.debug(ctxMsg(ctx, msg)))
  def trace(msg: => String)(implicit ctx: Ctx, l: Logger): UIO[Unit]               = UIO(l.trace(ctxMsg(ctx, msg)))
  def error(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit] = UIO(l.error(ctxMsg(ctx, msg), t))
  def warn(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit]  = UIO(l.warn(ctxMsg(ctx, msg), t))
  def info(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit]  = UIO(l.info(ctxMsg(ctx, msg), t))
  def debug(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit] = UIO(l.debug(ctxMsg(ctx, msg), t))
  def trace(msg: => String, t: Throwable)(implicit ctx: Ctx, l: Logger): UIO[Unit] = UIO(l.trace(ctxMsg(ctx, msg), t))
}

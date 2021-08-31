package ua.kpi.ipsa.config

import zio.{Has, TaskLayer, ZIO}

import scala.util.control.NoStackTrace

case class AppConfig(db: DBConfig, server: AppHttpServerConf)
case class AppHttpServerConf(port: Int)
case class DBConfig(
    server: String,
    database: String,
    user: String,
    password: String,
    schema: Option[String],
    port: Int,
    ssl: Boolean,
    poolSize: Int,
    connectionTimeout: Int,
    keepaliveTime: Int,
    maxLifetime: Int,
    socketTimeout: Int
)

object AppConfig {
  import pureconfig._
  import pureconfig.generic.auto._
  val live: TaskLayer[Has[AppConfig]] = {
    ZIO
      .fromEither(ConfigSource.default.load[AppConfig])
      .foldM(
        err => ZIO.fail(new IllegalArgumentException(s"config error: $err") with NoStackTrace),
        v => ZIO(v)
      )
      .toLayer
  }
}

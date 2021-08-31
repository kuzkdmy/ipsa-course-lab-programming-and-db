package ua.kpi.ipsa

import doobie.Transactor
import doobie.implicits._
import zio.interop.catz._
import zio.{Has, Task, ZIO}

package object repository {
  type TranzactIO[T] = ZIO[Has[Transactor[Task]], Throwable, T]

  def tzio[T](c: doobie.ConnectionIO[T]): TranzactIO[T] = {
    for {
      tx  <- ZIO.service[Transactor[Task]]
      res <- c.transact(tx)
    } yield res
  }
}

import io.debezium.engine.format.Json
import io.debezium.engine.{ChangeEvent, DebeziumEngine}
import zio.Has
import zio._
import zio.stream.ZStream
import zio.blocking.{Blocking, blocking, effectBlocking}

import java.util.Properties
import java.util.concurrent.LinkedBlockingQueue

trait DebezioEngine {
  def consume(): ZStream[Blocking, Throwable, ChangeEvent[String, String]]
  val run: URIO[Blocking, Unit]
  val close: URIO[Blocking, Unit]
}

object DebezioEngine {

  def make(settings: Properties): ZManaged[Blocking, Throwable, DebezioEngine] =
      blocking {
        ZIO(Live(settings))
      }.toManaged(_.close)

  final case class Live(private val settings: Properties) extends DebezioEngine {

    private val blockingQueue = new LinkedBlockingQueue[ChangeEvent[String, String]]()

    private val engine = for {
      de <- UIO(DebeziumEngine
          .create(classOf[Json])
          .using(settings)
          .notifying(record => blockingQueue.put(record))
          .build())
    } yield (de, blockingQueue)

    override val run: URIO[Blocking, Unit] = blocking(engine.map(_._1.run()))

    override val close: URIO[Blocking, Unit] = blocking(engine.map(_._1.close()))

    override def consume(): ZStream[Blocking, Throwable, ChangeEvent[String, String]] = for {
      bq <- ZStream.fromEffect(run.fork *> engine.map(_._2))
      changeEvent <- ZStream.repeatEffect(effectBlocking(bq.take()))
    } yield changeEvent
  }

  def consume(): URIO[Has[DebezioEngine], ZStream[Blocking, Throwable, ChangeEvent[String, String]]] =
    ZIO.service[DebezioEngine].map(_.consume())
}

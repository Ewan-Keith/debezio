import zio._

import java.util.Properties
import zio.console.putStrLn
import zio.stream.ZStream

sealed trait ConnectorClass { val classPath: String }
case object PostgresConnector extends ConnectorClass {
    override val classPath: String = "io.debezium.connector.postgresql.PostgresConnector"
}

case class DebeziumSettings(
  connectorClass: ConnectorClass) {

    def withConnectorClass(connectorClass: ConnectorClass): DebeziumSettings =
        copy(connectorClass = connectorClass)
}


object Main extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

  val props = new Properties()
  props.setProperty("name", "engine")
  props.setProperty("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
  props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
  props.setProperty("offset.storage.file.filename", "/tmp/offsets.dat")
  props.setProperty("offset.flush.interval.ms", "60000")
  props.setProperty("database.hostname", "localhost")
  props.setProperty("database.port", "5432")
  props.setProperty("database.user", "postgres")
  props.setProperty("database.password", "devpw")
  props.setProperty("database.dbname", "postgres")
  props.setProperty("database.server.name", "debezio-dev")

    for {
      _ <- putStrLn("starting for comprehension")
      dbe = DebezioEngine.make(props)
      dbel = ZLayer.fromManaged(dbe)
      zs <- DebezioEngine.consume().provideLayer(dbel).catchAll(_ => ZIO.succeed(ZStream.empty))
      _ <- zs.foreach(c => putStrLn(c.toString)).ignore
      _ <- putStrLn("after engine run")
    } yield ExitCode.success
  }
}
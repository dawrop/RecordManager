package http

import slick.jdbc.PostgresProfile.api._
import com.typesafe.config.{ Config, ConfigFactory }
import infrastructure.tables.RecordsTable.records

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object DbSetup {
  private def dbConnection(config: Config) = {
    val host     = config.getString("db.host")
    val port     = config.getString("db.port")
    val database = config.getString("db.database")
    val user     = config.getString("db.user")
    val pass     = config.getString("db.password")

    val jdbcUrl = s"jdbc:postgresql://$host:$port/$database"

    Database.forURL(jdbcUrl, user = user, password = pass)
  }

  def setup() = {
    val config = ConfigFactory.load("application.conf")
    val db     = dbConnection(config)

    val schemaInit = records.schema.createIfNotExists
    Await.result(db.run(schemaInit), 5.seconds)

    val createIndexes = DBIO.seq(
      sqlu"""CREATE INDEX IF NOT EXISTS idx_phone_number ON records (phone_number)""",
      sqlu"""CREATE INDEX IF NOT EXISTS idx_processed_at ON records (processed_at)"""
    )
    Await.result(db.run(createIndexes), 5.seconds)

    db
  }
}

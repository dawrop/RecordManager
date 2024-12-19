package commons

import com.typesafe.config.{ Config, ConfigFactory }

import scala.jdk.CollectionConverters.ListHasAsScala

object PrioritiesConfigLoader {
  case class Priority(range: (BigDecimal, BigDecimal), priority: Int)
  case class PrioritiesList(priorities: List[Priority])

  private def parseRange(range: String): (BigDecimal, BigDecimal) = {
    val Array(start, end) = range.split("-").map(_.trim)
    val startValue        = BigDecimal(start)
    val endValue          = if (end == "*") BigDecimal(Long.MaxValue) else BigDecimal(end)
    (startValue, endValue)
  }

  def loadPriorities(): PrioritiesList = {
    val config: Config = ConfigFactory.load("application.conf")

    val priorities = config
      .getConfigList("app.priorities")
      .asScala
      .map { priorityConfig =>
        val range    = parseRange(priorityConfig.getString("range"))
        val priority = priorityConfig.getInt("priority")
        Priority(range, priority)
      }
      .toList

    PrioritiesList(priorities)
  }
}

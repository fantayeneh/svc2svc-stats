package svc2svc

import com.twitter.finagle.stats.{Counter, Stat, StatsReceiver}
import com.twitter.util.{Duration, Return, Throw, Try}

trait Svc2SvcStats {
  def count(duration: Duration): Unit
}

object Svc2SvcStats {
  def apply(stats: StatsReceiver, route: SvcRoute, statusCode: Try[StatusCode]): Svc2SvcStats = {
    val s2sStats     = stats.scope("src").scope(route.src)
    val requestCount = s2sStats.counter("requests")
    val requestTime  = s2sStats.stat("time")
    val status       = s2sStats.scope("status")

    statusCode match {
      case Return(code) ⇒
        val statusClass = s"${code / 100}XX"
        HttpStats(requestCount, status.counter(code.toString), status.counter(statusClass), requestTime)
      case Throw(_) ⇒ ErrorStats(requestCount, status.counter("error"), requestTime)
    }
  }
}

case class HttpStats(requestCount: Counter, statusCodeCount: Counter, statusClassCount: Counter, requestTime: Stat)
    extends Svc2SvcStats {
  def count(duration: Duration): Unit = {
    requestCount.incr()
    statusCodeCount.incr()
    statusClassCount.incr()
    requestTime.add(duration.inMilliseconds.toFloat)
  }
}

case class ErrorStats(requestCount: Counter, errorCount: Counter, requestTime: Stat) extends Svc2SvcStats {
  def count(duration: Duration): Unit = {
    requestCount.incr()
    errorCount.incr()
    requestTime.add(duration.inMilliseconds.toFloat)
  }
}

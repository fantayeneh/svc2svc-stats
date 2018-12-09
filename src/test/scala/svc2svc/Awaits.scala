package svc2svc

import com.twitter.conversions.time._
import com.twitter.util.{Await, Duration, Future, TimeoutException}
import org.scalatest.exceptions.TestFailedException

trait Awaits {
  def await[T](f: => Future[T]): T =
    await(2.seconds)(f)

  def await[T](t: Duration)(f: => Future[T]): T =
    try Await.result(f, t)
    catch {
      case cause: TimeoutException =>
        throw new TestFailedException(s"operation timed out after $t", cause, 5)
    }
}

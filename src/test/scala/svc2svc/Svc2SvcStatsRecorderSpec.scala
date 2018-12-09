package svc2svc

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.stats.{InMemoryStatsReceiver}
import com.twitter.util.{Future, Try}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalacheck._
import org.scalatest.prop.PropertyChecks

class Svc2SvcStatsRecorderSpec extends WordSpec with PropertyChecks with Matchers with Awaits with BeforeAndAfterEach {
  import Svc2SvcStatsRecorderSpec._

  var stats: InMemoryStatsReceiver = _
  var plugin: Svc2SvcStatsRecorder = _

  override protected def beforeEach(): Unit = {
    stats = new InMemoryStatsReceiver
    plugin = new Svc2SvcStatsRecorder(stats)
  }

  private def requestCounts(req: Request, resp: Option[Response]) = {
    val errOrStatus = resp.fold("error")(r ⇒ s"${r.statusCode / 100}XX")
    (stats.counter("src", "src-service", "requests")(),
     stats.counter("src", "src-service", "status", s"$errOrStatus")())
  }

  "svc 2 svc stats plugin" should {
    "reports service-to-service stats" in {
      forAll(genRequest, genResponse) { (req, resp) ⇒
        val backend                    = Service.const(Future.value(resp))
        val service                    = plugin andThen backend
        val (currRequests, currStatus) = requestCounts(req, Some(resp))
        await(service(req))

        val (requests, status) = requestCounts(req, Some(resp))
        requests shouldBe (currRequests + 1)
        status shouldBe (currStatus + 1)
      }
    }

    "skip recording if src or dst are missing" in {
      val resp    = Response(Status.Ok)
      val service = plugin andThen Service.const(Future.value(resp))

      val req = Request("/")
      await(service(req))
      val (status, requests) = requestCounts(req, Some(resp))
      status shouldBe 0
      requests shouldBe 0
    }

    "show error if response is not present" in {
      val service = plugin andThen Service.const(Future.exception(new RuntimeException("network")))
      val req     = genRequest.sample.get
      Try(await(service(req)))
      val errorCount = stats.counter("src", "src-service", "status", "error")()
      errorCount shouldBe (1)
    }
  }
}

object Svc2SvcStatsRecorderSpec {
  implicit class RichRequest(req: Request) {
    def srcService(src: String): Request = {
      req.headerMap.add(SrcSvcHeader, src)
      return req
    }

    def dstService(src: String): Request = {
      req.headerMap.add(DstSvcHeader, src)
      return req
    }

  }

  val genRequest: Gen[Request] = for {
    method ← Gen.oneOf(Method.Get, Method.Post, Method.Delete)
  } yield
    Request(method, "/")
      .srcService("src-service")
      .dstService("dst-service")

  val genResponse: Gen[Response] = for {
    status ← Gen.choose(100, 500).map(Status.fromCode(_))
  } yield Response(status)

}

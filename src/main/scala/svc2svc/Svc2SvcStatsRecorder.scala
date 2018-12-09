package svc2svc

import com.twitter.finagle.http._
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util._

final case class SvcRoute(src: SrcSvc, dst: DstSvc, method: Method)

class Svc2SvcStatsRecorder(stats: StatsReceiver, srcSvcHeader: String = SrcSvcHeader)
    extends SimpleFilter[Request, Response] {

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val elapsed = Stopwatch.start()
    service(request).respond { record(request, _, elapsed()) }
  }

  private def record(req: Request, resp: Try[Response], duration: Duration) =
    for {
      route ← route(req)
      stats = svc2svcStats(route, resp.map(_.statusCode))
      _     = stats.count(duration)
    } yield ()

  def route(req: Request) =
    for {
      src    ← req.headerMap.get(srcSvcHeader)
      dst    ← req.headerMap.get(DstSvcHeader)
      method = req.method
    } yield SvcRoute(src, dst, method)

  private val svc2svcStats = Memoize[(SvcRoute, Try[StatusCode]), Svc2SvcStats] {
    case (route, sc) ⇒ Svc2SvcStats(stats, route, sc)
  }

}

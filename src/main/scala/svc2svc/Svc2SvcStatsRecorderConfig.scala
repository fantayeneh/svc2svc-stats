package svc2svc

import com.fasterxml.jackson.annotation.JsonIgnore
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Stack, param ⇒ fParam}
import io.buoyant.linkerd.RequestAuthorizerInitializer
import io.buoyant.linkerd.protocol.HttpRequestAuthorizerConfig

class Svc2SvcStatsRecorderInitializer extends RequestAuthorizerInitializer {
  val configClass       = classOf[Svc2SvcStatsRecorderConfig]
  override val configId = Svc2SvcStatsRecorderConfig.kind
}

object Svc2SvcStatsRecorderInitializer extends Svc2SvcStatsRecorderInitializer

object Svc2SvcStatsRecorderConfig {
  val kind          = "com.homeaway.l5d.svc2SvcStatsRecorder"
  val defaultHeader = SrcSvcHeader
}

case class Svc2SvcStatsRecorderConfig(header: Option[String] = None) extends HttpRequestAuthorizerConfig {

  @JsonIgnore
  override def role = Stack.Role("Svc2SvcStatsRecorder")

  @JsonIgnore
  override def description = "Records stats by src-to-dst"

  @JsonIgnore
  override def parameters = Seq()

  @JsonIgnore
  def mk(params: Stack.Params): Filter[Request, Response, Request, Response] = {
    val stats            = params[fParam.Stats].statsReceiver
    val srcServiceHeader = header.getOrElse(Svc2SvcStatsRecorderConfig.defaultHeader)
    new Svc2SvcStatsRecorder(stats, srcServiceHeader)
  }
}

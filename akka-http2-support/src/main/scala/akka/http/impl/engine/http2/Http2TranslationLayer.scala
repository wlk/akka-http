package akka.http.impl.engine.http2

import akka.http.impl.engine.ws.InternalCustomHeader
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.util.ByteString

// TODO: public API, move to model
case class Http2SubstreamId(id: Int) extends InternalCustomHeader("x-http2-substream-id") {
  override def value: String = id.toString
}

// FIXME: replace with actual implementation
object Http2TranslationLayer {
  def rendering() =
    Flow[HttpResponse].map { resp ⇒
      val subid = resp.header[Http2SubstreamId].get.id
      Http2SubStream(HeadersFrame(subid, true, true, ByteString()), Source.empty)
    }
  def parsing() =
    Flow[Http2SubStream].map {
      case sub ⇒
        HttpRequest(uri = s"/${sub.streamId}", headers = Http2SubstreamId(sub.streamId) :: Nil)
    }
}

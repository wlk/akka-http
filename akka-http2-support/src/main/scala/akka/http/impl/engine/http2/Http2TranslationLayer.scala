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
      Http2SubStream(HeadersFrame(subid, true, true, exampleResponse), Source.empty)
    }
  def parsing() =
    Flow[Http2SubStream].map {
      case sub ⇒
        HttpRequest(uri = s"/${sub.streamId}", headers = Http2SubstreamId(sub.streamId) :: Nil)
    }

  /**
   * Example response from the HPACK spec, encoding this sequence of headers:
   *
   * :status: 302
   * cache-control: private
   * date: Mon, 21 Oct 2013 20:13:21 GMT
   * location: https://www.example.com
   */
  val exampleResponse: ByteString =
    ByteString(
      """4803 3330 3258 0770 7269 7661 7465 611d
         4d6f 6e2c 2032 3120 4f63 7420 3230 3133
         2032 303a 3133 3a32 3120 474d 546e 1768
         7474 7073 3a2f 2f77 7777 2e65 7861 6d70
         6c65 2e63 6f6d                          """.replaceAll("\\s", "")
      .grouped(2).map(java.lang.Byte.parseByte(_, 16)).toArray
    )
}

import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{APIMessage, ClientSettings}
import cats.implicits._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

object Main extends App {

  val tokenFilename = "token.txt"
  val stream = Source.fromFile(tokenFilename)
  val token = stream.getLines().collectFirst { case x => x }
  stream.close()

  val clientSettings = ClientSettings(token.get)
  import clientSettings.executionContext
  val client = Await.result(clientSettings.createClient(), Duration.Inf)

  client.onEventAsync { implicit c => {
    case APIMessage.MessageCreate(message, _) =>
      val response = CreateMessage(message.channelId, CreateMessageData(content = "Hi there!"))
      client.requestsHelper.run(response).map(_ => ())
    }
  }

  client.login()
}
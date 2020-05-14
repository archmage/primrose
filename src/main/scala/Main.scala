import ackcord.data.GuildMessage
import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{APIMessage, ClientSettings, EventRegistration}
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
    case APIMessage.MessageCreate(message, _) if message.content.toLowerCase.contains("primrose") =>

      // This is really bad!!!
      val guildMessage = message.asInstanceOf[GuildMessage]

      

      val messageData = CreateMessageData(content = s"Hi, ${guildMessage.member.nick.get.split(" ").head}!")
      val response = CreateMessage(message.channelId, messageData)
      client.requestsHelper.run(response).map(_ => ())
    }
  }

  client.login()
}
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import ackcord.cachehandlers.CacheSnapshotBuilder
import ackcord.data.{ChannelId, GuildMessage, RoleId, TextChannelId}
import ackcord.requests.{AddGuildMemberRole, CreateMessage, CreateMessageData, GetChannel}
import ackcord.{APIMessage, CacheSnapshot, CacheState, ClientSettings, EventRegistration}
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
  val client = Await.result(clientSettings.createClient(), Duration(15, "second"))

  client.onEventAsync { implicit c => {
    case APIMessage.MessageCreate(message, _) if message.isInstanceOf[GuildMessage] &&
     message.content.toLowerCase.contains("primrose") =>

      val guildMessage = message.asInstanceOf[GuildMessage]

      val name = guildMessage.member.nick.getOrElse(message.authorUser.get.username)

      val messageData = CreateMessageData(content = s"Hi, ${name.split(" ").head}!")
      val response = CreateMessage(message.channelId, messageData)
      client.requestsHelper.run(response).map(_ => ())
    }
  }

  client.onEventAsync { implicit c => {
    case APIMessage.MessageCreate(message, _) if message.isInstanceOf[GuildMessage] &&
      message.channelId == TextChannelId(707560505903939675L) =>

      val guildMessage = message.asInstanceOf[GuildMessage]

      val roleId = RoleId(717506376426979448L)
      val response = AddGuildMemberRole(guildMessage.guildId, message.authorUserId.get, roleId)

      client.requestsHelper.run(response).map(_ => ())
  }}

  client.login()

  // -- dumb stuff below --


  val messageData = CreateMessageData(content = s"_squeak_")
  val timedResponse = CreateMessage(TextChannelId(707560505903939675L), messageData)

  new Thread {
    override def run: Unit = {
      while(true) {
        Thread.sleep(10000)
        client.requests.single(timedResponse).map(_ => ())
        println(LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne")))
      }
    }
  }.run

}
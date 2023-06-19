package i_tools_sms_viewer

import java.sql.{Connection, DriverManager, ResultSet}

import java.time._
import java.time.format.DateTimeFormatter

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._

import scalafx.scene.control.cell.TextFieldListCell
import scalafx.util.StringConverter
import scalafx.Includes._
import scalafx.collections.ObservableBuffer

case class Message(message_id: String, date: String, is_from_me: Boolean, text: String)
case class Subscriber(number: String, messages: List[Message])


object main extends JFXApp3 {
  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "iToolsSmsViewer"

      scene = new Scene(800, 570) {
        var currentContact: Option[Subscriber] = None

        val contacts: ListView[Subscriber] = new ListView(fetchSubscribersFromDatabase()) {
          cellFactory = TextFieldListCell.forListView(
            new StringConverter[Subscriber] {
              def toString(subscriber: Subscriber): String = subscriber.number

              def fromString(string: String): Subscriber = ???
            }
          )
        }

        val messages: ListView[Message] = new ListView[Message]() {
          cellFactory = TextFieldListCell.forListView(
            new StringConverter[Message] {
              def toString(message: Message): String = s"${message.date}: ${message.text}"

              def fromString(string: String): Message = ???
            }
          )
        }

        contacts.selectionModel().selectedItemProperty.onChange { (_, _, newSubscriber) =>
          currentContact = Option(newSubscriber)
          currentContact match {
            case Some(subscriber) => messages.items = ObservableBuffer(subscriber.messages: _*)
            case None => messages.items = ObservableBuffer()
          }
        }

        // Display settings
        messages.layoutX = 200

        content = List(contacts, messages)
      }
    }
  }

  def fetchSubscribersFromDatabase(): List[Subscriber] = {
    Class.forName("org.sqlite.JDBC")
    val connectionUrl = "jdbc:sqlite:./sms.db"
    val connection: Connection = DriverManager.getConnection(connectionUrl)

    var subscribers = Map[String, Subscriber]()

    try {
      val statement = connection.createStatement()
      val queryGetAll =
        """
        SELECT chat.ROWID as subscriber_id,
        chat.chat_identifier AS number,
        message.ROWID AS message_id,
        message.date AS date,
        message.text AS text,
        message.is_from_me AS is_from_me
        FROM chat_message_join
        INNER JOIN chat
        ON chat_message_join.chat_id = chat.ROWID
        INNER JOIN message
        ON chat_message_join.message_id = message.ROWID
        ORDER BY subscriber_id, date ASC
      """

      val resultSet: ResultSet = statement.executeQuery(queryGetAll)

      while (resultSet.next()) {
        val number = resultSet.getString("number")
        val messageId = resultSet.getString("message_id")
        val date = convertDate(resultSet.getString("date"))
        val text = resultSet.getString("text")
        val isFromMe = resultSet.getBoolean("is_from_me")

        val message = Message(messageId, date, isFromMe, text)

        subscribers.get(number) match {
          case Some(subscriber) =>
            val updatedSubscriber = subscriber.copy(messages = subscriber.messages :+ message)
            subscribers = subscribers + (number -> updatedSubscriber)
          case None =>
            subscribers = subscribers + (number -> Subscriber(number, List(message)))
        }
      }
    } catch {
      case e: Exception => e.printStackTrace
    } finally {
      connection.close()
    }

    subscribers.values.toList
  }

  def filterSubscribers(subscribers: List[Subscriber], search: String): List[Subscriber] = {
    subscribers.filter { subscriber =>
      subscriber.messages.exists { message =>
        message.date.contains(search) || message.text.contains(search)
      }
    }
  }

  def convertDate(date: String): String = {
    val origin = LocalDateTime.of(2000, 12, 26, 4, 0, 0)
    val zoneId = ZoneId.of("UTC")
    val zonedOrigin = origin.atZone(zoneId)
    val duration = Duration.ofSeconds(date.toLong)
    val target = zonedOrigin.plus(duration)
    DateTimeFormatter.RFC_1123_DATE_TIME.format(target)
  }
}

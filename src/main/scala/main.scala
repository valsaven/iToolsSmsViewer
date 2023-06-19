package i_tools_sms_viewer

import java.sql.{Connection, DriverManager, ResultSet}
import java.time._
import java.time.format.DateTimeFormatter
import javafx.util.Callback

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox, Priority}
import scalafx.collections.ObservableBuffer
import scalafx.Includes._
import scalafx.scene.control.cell.TextFieldListCell
import scalafx.util.StringConverter

case class Message(message_id: String, date: String, is_from_me: Boolean, text: String)
case class Subscriber(number: String, messages: List[Message])

object main extends JFXApp3 {
  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "iToolsSmsViewer"
      resizable = false

      scene = new Scene {
        val allSubscribers: List[Subscriber] = fetchSubscribersFromDatabase()
        var currentContact: Option[Subscriber] = None

        val searchField: TextField = new TextField {
          layoutY = 10
          layoutX = 10
          text.onChange { (_, _, newSearch) =>
            val filtered = filterSubscribers(allSubscribers, newSearch)
            contacts.items = ObservableBuffer(filtered: _*)
            currentContact match {
              case Some(subscriber) if !filtered.contains(subscriber) =>
                currentContact = None
                messages.items = ObservableBuffer()
              case _ =>
            }
          }
        }

        val contacts: ListView[Subscriber] = new ListView[Subscriber] {
          items = ObservableBuffer(allSubscribers: _*)
          cellFactory = TextFieldListCell.forListView(
            new StringConverter[Subscriber] {
              def toString(subscriber: Subscriber): String = subscriber.number

              def fromString(string: String): Subscriber = ???
            }
          )
        }

        val messages: ListView[Message] = new ListView[Message]() {
          cellFactory = { (_: ListView[Message]) =>
            new ListCell[Message] {
              item.onChange { (_, _, message) =>
                graphic = message match {
                  case null => null
                  case msg =>
                    val dateLabel = new Label {
                      text <== createStringBinding(() => Option(item()).flatMap(m => Option(m.date)).getOrElse(""), item)
                      style = "-fx-font-size: small; -fx-text-fill: #007aff; -fx-font-weight: bold"
                    }
                    val textLabel = new Label {
                      text <== createStringBinding(() => Option(item()).flatMap(m => Option(m.text)).getOrElse(""), item)
                      style = "-fx-font-size: 16px"
                    }
                    val vbox = new VBox {
                      children = List(dateLabel, textLabel)
                      style =
                        s"""
                           | -fx-border-radius: 5px;
                           | -fx-font-size: 16px;
                           | -fx-margin: 8px;
                           | -fx-padding: 13px 14px;
                           | -fx-background-color: ${if (msg.is_from_me) "#93d841" else "#d3d3d3"};
                        """.stripMargin
                    }
                    val hbox = new HBox {
                      children = List(vbox)
                      alignment = if (msg.is_from_me) scalafx.geometry.Pos.CenterRight else scalafx.geometry.Pos.CenterLeft
                    }
                    hbox
                }
              }
            }
          }
        }

        contacts.selectionModel().selectedItemProperty.onChange { (_, _, newSubscriber) =>
          currentContact = Option(newSubscriber)
          currentContact match {
            case Some(subscriber) => messages.items = ObservableBuffer(subscriber.messages: _*)
            case None => messages.items = ObservableBuffer()
          }
        }

        // Display settings
        val mainPane: HBox = new HBox {
          children = Seq(contacts, messages)
          HBox.setHgrow(messages, Priority.Always)
        }

        val rootPane: VBox = new VBox {
          children = Seq(searchField, mainPane)
          VBox.setVgrow(mainPane, Priority.Always)
        }

        content = rootPane
      }
    }
  }

  private def fetchSubscribersFromDatabase(): List[Subscriber] = {
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

  private def filterSubscribers(subscribers: List[Subscriber], search: String): List[Subscriber] = {
    val lowerSearch = search.toLowerCase
    subscribers.filter { subscriber =>
      subscriber.messages.exists { message =>
        message.date.toLowerCase.contains(lowerSearch) || message.text.toLowerCase.contains(lowerSearch)
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

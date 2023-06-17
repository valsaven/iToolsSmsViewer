package i_tools_sms_viewer

import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.text.Text

import java.sql.{Connection, DriverManager, ResultSet}

object main extends JFXApp3 {
  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      //    initStyle(StageStyle.Unified)
      title = "iToolsSmsViewer"
      scene = new Scene {
        fill = Color.rgb(38, 38, 38)
        content = new HBox {
          padding = Insets(50, 80, 50, 80)

          children = Seq(
            new Text {
              text = fetchTextFromDatabase()
              style = "-fx-font: normal bold 20pt sans-serif"
              fill = new LinearGradient(
                endX = 0,
                stops = Stops(Red, DarkRed))
            },

            new Text {
              text = "!!!"
              style = "-fx-font: italic bold 20pt sans-serif"
              fill = new LinearGradient(
                endX = 0,
                stops = Stops(White, DarkGray)
              )
              effect = new DropShadow {
                color = DarkGray
                radius = 15
                spread = 0.25
              }
            }
          )
        }
      }
    }
  }

  def fetchTextFromDatabase(): String = {
    Class.forName("org.sqlite.JDBC")

    val connectionUrl = "jdbc:sqlite:./sms.db"
    val connection: Connection = DriverManager.getConnection(connectionUrl)

    var result = ""
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

      val stringBuilder = new StringBuilder

      while (resultSet.next()) {
        val subscriberId = resultSet.getString("subscriber_id")
        val number = resultSet.getString("number")
        val messageId = resultSet.getString("message_id")
        val date = resultSet.getString("date")
        val text = resultSet.getString("text")
        val isFromMe = resultSet.getString("is_from_me")

        stringBuilder.append(s"Subscriber ID: $subscriberId, Number: $number, Message ID: $messageId, Date: $date, Text: $text, Is From Me: $isFromMe\n")
      }
      result = stringBuilder.toString()
    } catch {
      case e: Exception => e.printStackTrace
    } finally {
      connection.close()
    }
    result
  }
}

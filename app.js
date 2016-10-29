const sqlite3 = require('sqlite3').verbose();

const db = new sqlite3.Database('sms.db');
const app = angular.module('smsViewer', []);

app.controller('mainCtrl', ($scope) => {
  $scope.subscribers = [];

  // Loader
  $scope.totalDisplayed = 20;
  $scope.loadMore = () => $scope.totalDisplayed += 20;

  function convertDate(row) {
    const date = new Date();
    // Date starts from 12/26/2000 04:00:00
    const sum = (977803200 + row.date) * 1000;
    date.setTime(sum);
    row.date = date.toUTCString();
    $scope.subscribers.push(row);
  }

  db.all('SELECT chat.ROWID as subscriber_id,' +
    'chat.chat_identifier AS number,' +
    'message.ROWID AS message_id,' +
    'message.date AS date,' +
    'message.text AS text,' +
    'message.is_from_me AS is_from_me ' +
    'FROM chat_message_join ' +
    'INNER JOIN chat ' +
    'ON chat_message_join.chat_id = chat.ROWID ' +
    'INNER JOIN message ' +
    'ON chat_message_join.message_id = message.ROWID ' +
    'ORDER BY subscriber_id, date ASC', (err, rows) => {
    rows.forEach((row) => {
      convertDate(row);

      return $scope.subscribers;
    });
  });

  db.close();
});

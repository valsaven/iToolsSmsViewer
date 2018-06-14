var angular = require('angular');
const sqlite3 = require('sqlite3').verbose();

const app = angular.module('smsViewer', []);
const db = new sqlite3.Database('sms.db');

app.controller('mainCtrl', $scope => {
  db.all(queryGetAll, (err, rows) => {
    const sub = [];

    rows.map(row => (row.date = convertDate(row.date)));

    $scope.subscribers = rows.reduce((res, s) => {
      if (sub[s.number] !== undefined) {
        res[sub[s.number]].messages.push({
          message_id: s.message_id,
          date: s.date,
          is_from_me: s.is_from_me,
          text: s.text
        });
      } else {
        res.push({
          number: s.number,
          messages: [
            {
              message_id: s.message_id,
              date: s.date,
              is_from_me: s.is_from_me,
              text: s.text
            }
          ]
        });
        sub[s.number] = res.length - 1;
      }
      return res;
    }, []);
  });

  db.close();

  // Message type
  $scope.type = m => (m ? 'sent' : 'received');

  /**
   * Select subscriber and show his messages
   * @param subscriber
   * @return {*}
   */
  $scope.selectSubscriber = subscriber => {
    $scope.subscriber = subscriber;

    return $scope.subscriber;
  };
});

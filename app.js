'use strict';

var angular = require('angular');
const sqlite3 = require('sqlite3').verbose();

const db = new sqlite3.Database('sms.db');
const app = angular.module('smsViewer', []);

app.controller('mainCtrl', ($scope) => {

  /**
   * Class representing subscribers with their messages.
   */
  class Subscribers {
    /**
     * @param {object} $scope - Current controller $scope
     */
    constructor($scope) {
      $scope.subscribers = [];

      /**
       * Convert a date from "354377899" to "Mon, 19 Mar 2012 18:18:19 GMT"
       * @param {string} date
       * @return {string}
       */
      function convertDate(date) {
        const d = new Date();
        // Date starts from 12/26/2000 04:00:00
        const sum = (977803200 + date) * 1000;
        d.setTime(sum);
        return d.toUTCString();
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
          rows.map((row) => row.date = convertDate(row.date));

          const sub = [];
          $scope.subscribers = rows.reduce((res, s) => {
            if (sub[s.number] !== undefined) {
              res[sub[s.number]].messages.push({
                message_id: s.message_id,
                date: s.date,
                is_from_me: s.is_from_me,
                text: s.text,
              });
            } else {
              res.push({
                number: s.number,
                messages: [{
                  message_id: s.message_id,
                  date: s.date,
                  is_from_me: s.is_from_me,
                  text: s.text,
                }],
              });
              sub[s.number] = res.length - 1;
            }
            return res;
          }, []);

          return $scope.subscribers;
        }
      );

      db.close();
    }
  }

  const subscribers = new Subscribers($scope);

  /**
   * Select subscriber and show his messages
   * @param subscriber
   * @return {*}
   */
  $scope.selectSubscriber = (subscriber) => {
    return $scope.subscriber = subscriber;
  };
});

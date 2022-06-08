<template>
  <div id="wrapper">
    <!-- Search -->
    <div class="search-box">
      <input
        id="search"
        v-model="search"
        type="text"
        name="search"
        placeholder="Search"
      >
    </div>

    <div class="container">
      <!-- Subscribers -->
      <div id="subscribers">
        <div
          v-for="(subscriber, index) in filteredSubscribers"
          :key="index"
          class="subscriber"
          @click="selectSubscriber(subscriber)"
        >
          <div class="title">
            <div class="number" v-text="subscriber.number"/>
          </div>
        </div>
      </div>
      <!-- Messages -->
      <div
        v-if="currentSubscriber"
        id="messages"
      >
        <message-block
          v-for="(message, index) in filteredMessages"
          :key="index"
          :message="message"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import MessageBlock from './MessageBlock.vue';
import { dbRow, Message, Subscriber } from '../types';

const sqlite3 = require('sqlite3').verbose();
const db = new sqlite3.Database('sms.db');


export default defineComponent({
  components: {
    MessageBlock,
  },

  data() {
    return {
      currentSubscriber: null as Subscriber | null,
      queryGetAll:
    'SELECT chat.ROWID as subscriber_id,'
    + 'chat.chat_identifier AS number,'
    + 'message.ROWID AS message_id,'
    + 'message.date AS date,'
    + 'message.text AS text,'
    + 'message.is_from_me AS is_from_me '
    + 'FROM chat_message_join '
    + 'INNER JOIN chat '
    + 'ON chat_message_join.chat_id = chat.ROWID '
    + 'INNER JOIN message '
    + 'ON chat_message_join.message_id = message.ROWID '
    + 'ORDER BY subscriber_id, date ASC',
      search: '',
      subscribers: [] as Subscriber[],
    };
  },

  computed: {
    filteredSubscribers(): Subscriber[] {
      return this.subscribers.filter((subscriber) => {
        const messages = subscriber.messages.filter((message: Message) => message.date.includes(this.search)
          || message.text.includes(this.search));
        return messages.length !== 0;
      });
    },

    filteredMessages(): Message[] {
      if (!this.currentSubscriber) {
        return [];
      }

      return this.currentSubscriber.messages.filter(message => {
        return message.date.includes(this.search) || message.text.includes(this.search)
      });
    },
  },

  methods: {
    /**
     * Convert a date from "354377899" to "Mon, 19 Mar 2012 18:18:19 GMT"
     * @param {string} date
     * @return {string}
     */
    convertDate(date: string) {
      const d = new Date();
      const sum = (977803200 + Number(date)) * 1000; // Date starts from 12/26/2000 04:00:00
      d.setTime(sum);

      return d.toUTCString().slice(0, -4);
    },

    selectSubscriber(subscriber: Subscriber) {
      this.currentSubscriber = subscriber;
    },
  },

  created() {
    db.all(this.queryGetAll, (err: any, rows: dbRow[]) => {
      rows.forEach((row) => {
        row.date = this.convertDate(row.date);
      });

      // @ts-ignore
      const sub: { [key: string]: number } = [];

      this.subscribers = rows.reduce((res: Subscriber[], row: dbRow) => {
        if (sub[row.number] != undefined) {
          res[sub[row.number]].messages.push({
            message_id: row.message_id,
            date: row.date,
            is_from_me: row.is_from_me,
            text: row.text,
          });
        } else {
          res.push({
            number: row.number,
            messages: [
              {
                message_id: row.message_id,
                date: row.date,
                is_from_me: row.is_from_me,
                text: row.text,
              },
            ],
          });
          sub[row.number] = res.length - 1;
        }
        return res;
      }, []);

      this.currentSubscriber = this.filteredSubscribers.length > 0
        ? this.filteredSubscribers[0]
        : null;
    });

    db.close();
  },
});
</script>

<style>
html,
body {
  margin: 0;

  font-family: sans-serif;
}

input:focus {
  outline: none;
}

.container {
  display: flex;
  flex-direction: row;
}

/* Search */
.search-box {
  padding: 0 10px;

  background-color: #bdc8cb;
}

#search {
  width: 95%;
  margin: 5px;
  padding: 5px;

  transition: box-shadow 0.3s, border 0.3s;

  border: solid 1px #dcdcdc;
  border-radius: 50px;
}

/* Subscribers List */
#subscribers {
  overflow: auto;
  flex: 1;
  flex-direction: column;

  height: 530px;
}

/* Subscriber */
.subscriber {
  flex-direction: column;

  padding: 8px 20px 2px;

  border-bottom: 1px solid #888;
}

.active {
  color: #fff;
  background-color: #0060dc;
}

.subscriber:hover {
  cursor: pointer;

  background-color: #007aff;
}

.subscriber:hover * {
  color: #fff;
}

.title {
  display: flex;
  flex-direction: row;

  padding-bottom: 5px;
}

.number {
  font-weight: bold;
}

.date {
  margin-left: auto;

  color: #007aff;

  font-size: small;
}

.lastSms {
  color: #858489;

  font-size: 14px;
}

/* Messages List */
#messages {
  overflow: auto;
  flex: 2;
  flex-direction: column;

  height: 530px;

  background-color: #dbe1ed;
}
</style>

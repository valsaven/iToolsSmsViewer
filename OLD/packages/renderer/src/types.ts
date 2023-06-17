/*
  Example:
    date: "Thu, 16 Jun 2016 12:07:20"
    is_from_me: 0
    message_id: 1
    number: "github"
    subscriber_id: 1
    text: "877910 is your GitHub authentication code."
*/
export interface dbRow {
  date: string;
  is_from_me: number;
  message_id: number;
  number: string;
  subscriber_id: number;
  text: string;
}

export interface Message {
  date: string;
  is_from_me: number;
  message_id: number;
  text: string;
}

export interface Subscriber {
  number: string;
  messages: Message[]
}
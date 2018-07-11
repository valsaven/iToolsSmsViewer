<template>
  <div
    :class="{
      sent: message.is_from_me,
      received: !message.is_from_me,
    }"
    class="message"
  >
    <div class="date">{{ message.date }}</div>
    <div class="text">{{ message.text }}</div>
  </div>
</template>

<script>
export default {
  name: 'Message',
  props: {
    message: {
      type: Object,
      default: () => {},
    },
  },
};
</script>

<style>
.message {
  border-radius: 5px;
  clear: both;
  display: inline-block;
  font-size: 16px;
  margin: 8px;
  max-width: 50%;
  padding: 13px 14px;
  position: relative;
  vertical-align: top;
}

.date {
  color: #007aff;
  font-size: small;
  margin-bottom: 4px;
  margin-left: auto;
}

.received {
  align-self: flex-start;
  background-color: #d3d3d3;
  float: left;
}

.sent {
  align-self: flex-end;
  background-color: #93d841;
  float: right;
}
</style>

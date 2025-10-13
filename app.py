# iToolsSmsViewer - SMS viewer for iTools Super Backup.
# Copyright (C) 2016-2025 Val Saven
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

import sqlite3
import tkinter as tk

from tkinter import ttk
from datetime import datetime, timezone

# Connection to DB
conn = sqlite3.connect('sms.db')
cursor = conn.cursor()

# SQL query
query = """
SELECT chat.ROWID as subscriber_id,
       chat.chat_identifier AS number,
       message.ROWID AS message_id,
       message.date AS date,
       message.text AS text,
       message.is_from_me AS is_from_me
FROM chat_message_join
INNER JOIN chat ON chat_message_join.chat_id = chat.ROWID
INNER JOIN message ON chat_message_join.message_id = message.ROWID
ORDER BY subscriber_id, date ASC
"""

# Data loading
rows = cursor.execute(query).fetchall()
conn.close()

# Date conversion
def convert_date(date_str):
    base_time = 977803200  # 2000-12-26 04:00:00
    timestamp = (base_time + int(date_str)) * 1000
    dt = datetime.fromtimestamp(timestamp / 1000, tz=timezone.utc)
    return dt.strftime('%a, %d %b %Y %H:%M:%S')

# Data processing: grouping by numbers
subscribers = {}
for row in rows:
    number = row[1]
    date = convert_date(row[3])
    message = {
        'message_id': row[2],
        'date': date,
        'text': row[4] or '',  # In case of None
        'is_from_me': row[5]
    }
    if number not in subscribers:
        subscribers[number] = {'number': number, 'messages': []}
    subscribers[number]['messages'].append(message)

subscribers_list = list(subscribers.values())
current_filtered_subscribers = []  # For storing the filtered list

# GUI
root = tk.Tk()
root.title("iTools SMS Viewer")
root.geometry("800x600")

# Search
search_var = tk.StringVar()
search_entry = ttk.Entry(root, textvariable=search_var)
search_entry.pack(pady=10, padx=10, fill='x')
search_entry.bind('<KeyRelease>', lambda e: update_lists())  # Update on input

# Container
frame = ttk.Frame(root)
frame.pack(fill='both', expand=True)

# Left: List of contacts
left_frame = ttk.Frame(frame, width=200)
left_frame.pack(side='left', fill='y', padx=10, pady=10)

subscriber_listbox = tk.Listbox(left_frame, width=30, height=30)
subscriber_listbox.pack(fill='y', expand=True)
subscriber_listbox.bind('<<ListboxSelect>>', lambda e: show_messages())

# Right: Messages
right_frame = ttk.Frame(frame)
right_frame.pack(side='right', fill='both', expand=True, padx=10, pady=10)

messages_text = tk.Text(right_frame, wrap='word', height=30)
messages_text.pack(fill='both', expand=True)

# Setting up tags for styles
messages_text.tag_config('date', foreground='blue')
messages_text.tag_config('sent', background='#93d841')  # Green for sent
messages_text.tag_config('received', background='#d3d3d3')  # Gray for received

# Update lists by search
def update_lists():
    global current_filtered_subscribers
    search = search_var.get().lower()
    subscriber_listbox.delete(0, tk.END)
    filtered_subscribers = []
    for sub in subscribers_list:
        filtered_msgs = [msg for msg in sub['messages'] if search in msg['date'].lower() or search in msg['text'].lower()]
        if filtered_msgs:
            filtered_subscribers.append(sub)
            subscriber_listbox.insert(tk.END, sub['number'])
    current_filtered_subscribers = filtered_subscribers
    if current_filtered_subscribers:
        subscriber_listbox.select_set(0)
        show_messages()

# Display messages for the selected one
def show_messages():
    sel = subscriber_listbox.curselection()
    if not sel:
        return
    sub = current_filtered_subscribers[sel[0]]
    search = search_var.get().lower()
    messages_text.delete('1.0', tk.END)
    for msg in sub['messages']:
        if search in msg['date'].lower() or search in msg['text'].lower():
            messages_text.insert(tk.END, f"{msg['date']}\n", 'date')
            tag = 'sent' if msg['is_from_me'] else 'received'
            messages_text.insert(tk.END, f"{msg['text']}\n\n", tag)

# Initialization
update_lists()

root.mainloop()

#!/bin/bash
sleep 10

kafka-topics --create \
  --bootstrap-server kafka1:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic delete-user-topic

echo "Топик delete-user-topic создан"

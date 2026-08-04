#!/usr/bin/env bash
# Creates the 4 ReconX Kafka topics directly against the reconx-kafka
# container, independent of the backend starting up.
#
# Normally Spring's KafkaAdmin creates these topics at backend startup from
# the NewTopic beans in KafkaTopicsConfig.java — but that only happens if the
# backend manages to start, and Kafka's own KAFKA_AUTO_CREATE_TOPICS_ENABLE
# only kicks in lazily, on first produce/consume, with the wrong partition
# count (1, not the 3/2/1/3 this project actually uses). This script lets you
# verify or pre-create the topics with the right partition counts before ever
# starting the backend — useful when just poking around in Kafdrop, or when
# debugging whether a problem is "Kafka isn't up" vs "topics don't exist yet".
#
# Usage: docker compose up -d zookeeper kafka   (wait for it to be healthy)
#        ./scripts/kafka-init-topics.sh

set -euo pipefail

CONTAINER="reconx-kafka"
BROKER="localhost:9092"

if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
    echo "Container '$CONTAINER' is not running. Start it first:"
    echo "  docker compose up -d zookeeper kafka"
    exit 1
fi

create_topic() {
    local name="$1" partitions="$2"
    echo "Creating topic '$name' (partitions=$partitions, replication=1)..."
    docker exec "$CONTAINER" kafka-topics \
        --bootstrap-server "$BROKER" \
        --create --if-not-exists \
        --topic "$name" \
        --partitions "$partitions" \
        --replication-factor 1
}

create_topic "trade-events"      3
create_topic "recon-results"     2
create_topic "system-alerts"     1
create_topic "trade-events-dlq"  3

echo ""
echo "Current topics on $BROKER:"
docker exec "$CONTAINER" kafka-topics --bootstrap-server "$BROKER" --list

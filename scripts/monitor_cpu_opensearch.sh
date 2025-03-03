#!/bin/bash

API_URL="http://localhost:9200/_nodes/stats/os"
INDEX_NAME="cpu_monitoring"

# Delete and recreate the index with correct mappings
curl -X DELETE "http://localhost:9200/$INDEX_NAME"

curl -X PUT "http://localhost:9200/$INDEX_NAME" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "timestamp": { "type": "date" },
      "cpu_percent_avg": { "type": "float" },
      "nodes": {
        "type": "nested",
        "properties": {
          "name": { "type": "keyword" },
          "cpu_percent": { "type": "float" }
        }
      }
    }
  }
}'

echo "Index $INDEX_NAME recreated successfully!"

# Monitor CPU every second for 600 seconds
for i in {1..600}
do
    TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

    # Fetch CPU usage per node and replace empty values with 0
    CPU_DATA=$(curl -s $API_URL | jq '.nodes | to_entries | map({name: .value.name, cpu_percent: (.value.os.cpu.percent // 0)})')

    # Ensure that CPU_DATA is not empty or malformed
    if [[ -z "$CPU_DATA" || "$CPU_DATA" == "[]" ]]; then
        echo "Warning: No CPU data found. Skipping this entry."
        continue
    fi

    # Extract CPU values correctly as an array
    CPU_VALUES=$(echo "$CPU_DATA" | jq '[.[].cpu_percent]')

    # Compute average CPU usage
    if [[ -z "$CPU_VALUES" || "$CPU_VALUES" == "[]" ]]; then
        CPU_AVG=0
    else
        CPU_AVG=$(echo "$CPU_VALUES" | jq 'add / length')
    fi

    # Format JSON correctly before sending to OpenSearch
    JSON_DATA=$(jq -n --arg ts "$TIMESTAMP" --argjson cpu_avg "$CPU_AVG" --argjson nodes "$CPU_DATA" \
    '{timestamp: $ts, cpu_percent_avg: $cpu_avg, nodes: $nodes}')

    # Send data to OpenSearch
    curl -X POST "http://localhost:9200/$INDEX_NAME/_doc" -H 'Content-Type: application/json' -d "$JSON_DATA"

    sleep 1
done

echo "CPU monitoring completed and stored in OpenSearch."

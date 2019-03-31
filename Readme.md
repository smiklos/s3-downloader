# Cloud Bagger File Downloader

## Requirements

1 sharded kinesis stream

Future work:

Introduce scaleability via multiple shards.

Local setup:

Make sure kinesis and s3 is ready

Run splitter via

sam local invoke --docker-network cloud-fun_cloud -e event.json FileSplitter

sam local start-api -p 5454 --docker-network cloud-fun_cloud

curl -H "Content-Type: application/json" --data @event.json http://127.0.0.1:5454/split

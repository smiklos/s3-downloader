# Cloud Bagger File Downloader

## Requirements

1 sharded kinesis stream

Future work:

Introduce scaleability via multiple shards.

Local setup:

Make sure kinesis and s3 is ready

Run splitter via

sam local invoke --docker-network cloud-fun_cloud -e event.json FileSplitter

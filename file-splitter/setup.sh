aws kinesis --profile cloud-server --endpoint-url http://localhost:4567 create-stream --stream-name test --shard-count 1 --region eu-west-1

aws s3api --profile cloud-server --endpoint-url http://localhost:8000 create-bucket --bucket infare-dev-test
aws s3 cp --profile cloud-server --endpoint-url http://localhost:8000 testbig.json s3://infare-dev-test/testbig.json

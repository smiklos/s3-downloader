# Cloud File Downloader

![Architecture](architecture.png "Architecture")

![Main flow](sequence.png "Main flow")


Local setup:

Install [docker](https://docs.docker.com/install/linux/docker-ce/ubuntu/) and [compose](https://linuxize.com/post/how-to-install-and-use-docker-compose-on-ubuntu-18-04/) 

Install [SAM Cli](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html) from AWS.


Boot up s3 and kinesis (local) via `docker-compose up -d`

Start SAM local api gateway

`cd file-splitter && sam local start-api -p 5454 --docker-network cloud-fun_cloud`

Start the downloader service via idea

Run `./setup.sh`

curl http://127.0.0.1:8081/download/testbig.json

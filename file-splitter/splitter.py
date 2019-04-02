import boto3
import datetime
import os
import json

if 'S3_HOST' in os.environ:
    s3 = boto3.resource('s3', endpoint_url = os.environ['S3_HOST'])
    kinesis = boto3.client('kinesis', endpoint_url = os.environ['KINESIS_HOST'])
else:
    s3 = boto3.resource('kinesis')
    kinesis = boto3.client('kinesis', endpoint_url = os.environ['KINESIS_HOST'])

MAX_RECORDS_PER_BATCH = 5
MAX_RECORD_SIZE_BYTES = 1024 * 1024 # 1Mb

def handle(event, context):
    body =  json.loads(event['body'])
    bucket = body['bucket']
    key = body['key']

    obj = s3.Object(bucket, key)
    object_stream = obj.get()['Body']


    batches = split_up(body['splits'], 5)
    for batch in batches:

        records = [_create_record(split['key'], object_stream.read(split['chunk'])) for split in batch]
        print("sending batch")
        response = kinesis.put_records(
                            Records = list(records),
                            StreamName = os.environ['STREAM_NAME']
                            )
    return {
     'statusCode': 200
    }

def split_up(l, n):
    for i in range(0, len(l), n):
        yield l[i:i + n]

def _create_record(key, chunk):
    return {
         'Data': chunk,
         'PartitionKey': key
     }

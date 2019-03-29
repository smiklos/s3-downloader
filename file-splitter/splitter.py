import boto3
import datetime
import os

if 'S3_HOST' in os.environ:
    s3 = boto3.resource('s3', endpoint_url = os.environ['S3_HOST'])
    kinesis = boto3.client('kinesis', endpoint_url = os.environ['KINESIS_HOST'])
else:
    s3 = boto3.resource('kinesis')
    kinesis = boto3.client('kinesis', endpoint_url = os.environ['KINESIS_HOST'])

MAX_RECORDS_PER_BATCH = 5
MAX_RECORD_SIZE_BYTES = 1024 * 1024 # 1Mb

def handle(event, context):
    obj = s3.Object(event['bucket'], event['key'])
    object_stream = obj.get()['Body']
    key_length = len(event['key'].encode())

    object_length = obj.content_length
    chunk_size = MAX_RECORD_SIZE_BYTES - key_length


    records = []
    for chunk in object_stream.iter_chunks(chunk_size = chunk_size):
        records.append({
            'Data': chunk,
            'PartitionKey': event['key']
        })

    response = kinesis.put_records(
                        Records=
                            records,
                        StreamName= os.environ['STREAM_NAME']
                        )

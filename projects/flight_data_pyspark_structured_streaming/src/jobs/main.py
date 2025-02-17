from pyspark.sql import SparkSession
from pyspark.sql.functions import *

KAFKA_BROKERS = "localhost:29092,localhost:39092,localhost:49092"
SOURCE_TOPIC = "financial_transactions"
AGGREGATES_TOPIC = "transaction_aggregates"
ANOMALIES_TOPIC = "transaction_anomalies"
CHECKPOINT_DIR = "/mnt/spark-checkpoints"
STATE_DIR = "/mnt/spark-state"

def main():
    pass

if __name__ == "__main__":
    main()
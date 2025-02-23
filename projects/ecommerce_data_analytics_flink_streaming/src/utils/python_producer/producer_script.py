import json
import time

from faker import Faker
import random
from confluent_kafka import SerializingProducer
from datetime import datetime, UTC

fake = Faker()

def generate_sales_transactions():
    user = fake.simple_profile()

    return {
        "transactionId": fake.uuid4(),
        "productId": random.choice(['product1', 'product2', 'product3', 'product4', 'product5', 'product6']),
        "productName": random.choice(['laptop', 'mobile', 'tablate', 'watch', 'headphone', 'speaker']),
        "productCategory": random.choice(['electronic', 'fashion', 'grocery', 'home', 'beauty', 'sports']),
        "productPrice": round(random.uniform(10, 1000), 2),
        "productQuantity": random.randint(1, 10),
        "productBrand": random.choice(['apple', 'samsung', 'oneplus', 'boat', 'sony']),
        "currency": random.choice(['USD', 'INR', 'GBP']),
        "customerId": user['username'],
        "transactionDate": datetime.utcnow().strftime('%Y-%m-%dT%H:%S.%f%z'),
        "paymentMethod": random.choice(["credit_card", "debit_card", "online_transfer"]),
    }

def delivery_report(err, msg):
    if err is not None:
        print(f"Message of delivery failure: {err}")
    else:
        print(f"Message delivered to {msg.topic} [{msg.partition()}]")

def main():
    topic = 'sales_transactions'
    producer = SerializingProducer({
        'bootstrap.servers': 'localhost:9092'
    })
    current_time = datetime.now()
    while (datetime.now() - current_time).seconds < 120:
        try:
            transaction = generate_sales_transactions()
            transaction['totalAmount'] = transaction['productPrice'] * transaction['productQuantity']
            print(transaction)
            producer.produce(
                topic,
                key=transaction['transactionId'],
                value=json.dumps(transaction),
                on_delivery=delivery_report
            )
            producer.poll(0)
            time.sleep(5)
        except BufferError:
            print("Buffer full ! Waiting...")
            time.sleep(1)
        except Exception as e:
            print(e)

if __name__ == "__main__":
    main()
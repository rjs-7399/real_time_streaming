## Ecommerce Data Analytics


### Commands for kafka message generation

- Run docker compose in terminal 1

```commandline
docker compose up -d
```

- Run python producer in terminal 2

```commandline
python3 producer_script.py
```

- Run broker container in terminal 3

```commandline
docker exec -it broker bash
```
```
# inside broker container
kafka-topics --list --bootstrap-server broker:29092
sales_transactions


kafka-console-consumer --topic sales_transactions --bootstrap-server localhost:9092

{"transactionId": "cd3fa541-9812-4dd7-b73b-49e6fef85e79", "productId": "product3", "productName": "headphone", "productCategory": "sports", "productPrice": 863.38, "productQuantity": 3, "productBrand": "samsung", "currency": "GBP", "customerId": "brownkristen", "transactionDate": "2025-02-21T06:40:433546+0000", "paymentMethod": "credit_card", "totalAmount": 2590.14}
{"transactionId": "e4457989-df7a-47b8-82cf-118c9c406897", "productId": "product5", "productName": "watch", "productCategory": "home", "productPrice": 785.7, "productQuantity": 2, "productBrand": "apple", "currency": "GBP", "customerId": "cwillis", "transactionDate": "2025-02-21T06:45:439499+0000", "paymentMethod": "debit_card", "totalAmount": 1571.4}

```
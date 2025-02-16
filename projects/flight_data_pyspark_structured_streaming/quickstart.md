## Flight Data Analytics

- We have created a docker compose file with all the relavant services. 
- Here we are planning to develop a pyspark streaming application at high scale.
- For that purpose we are adding three workers, for that we need to add three brockers and three controllers.
- At the first attempt we are trying with python producer script. But as python runs the code in single thread it is generating messages at low production rate.
- Python producer script generates 10k messages/30 second batch. And our goal is to reach 1.2 Billion messages per 1 hour production rate.
- So we need more efficient producer. As java supports multi-threading we will try out the producer in java.
- Even after implementing thread in python code we didn't see any huge performance improvement. It is still generating 1500 messages per second.
- So if we run python script to generate messages in kafka topics in 5 different terminals then each job will generate messages in 3 threads. Total it will be 15 threads. Even with this setup we are able to achieve only 3000 messages per second which is not satisfactory.
- Even the laptop caught heat.
- With 5 running kafka producer where each producer is having 3 threads we are able to achieve the rate of 8000 messages per seconds. 
- Which means from kafka producer source side we can produce 28.66 million messages in 1 hour. 
- And if we build an efficient pyspark pipeline which processes the same number of messages so at max we can produce 28.66 million messages.
- But our goal is to achieve 1.2 Billion. It's high time to move ahead with Java Producer.
- 
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
- While writing th code in pyspark structured streaming job we need to maintain the standards.
- We need to configure the exact version of pyspark. For that we need to go to [maven_repo](https://mvnrepository.com/).
- And search for `spark-sql-kafka`. We need to find the version that we are running in our docker and for that version we need to find the compatible package name in maven repo. And provide it in our code.
- We ran the docker compose file. We can check the spark version in `spark-master` in the docker container terminal.
- Here we have 3 workers running. They are not so capable enough to handle the high throughput rate.
- But still our pyspark streaming job is capable to process 154k records per second. At this rate if we stabilize this job then this can producer 553 million which is half a billion records per 1 hour.
- Here on the local system we are running everything in the docker container which runs on Mac m2 chip which is ofcourse not so capable enough to handle the heavy streaming operations.
- When we are working on the production grade system we must run the entire architecture on kubernetes rather than using docker container.
- In production grade systems we can run our pyspark streaming job on AWS EKS with multiple on demand instances with more processing power.
- And we can host the kafka topics in kubernetes deployments. This way it will become efficient and with this setup we can surely generate upto 1 billion records per hour.
- Volumes always stores some metadata of all the services that are running in the docker containers.
- When we log in to the grafana for the first time, it will ask for the credentials. Once you login using default creds that are admin, admin. it will ask to change your password. Once you save it, it will save creds into the volume.
- Next time when you run docker compose, it will fetch the metadata and same credentials will be valid. If we remove the volumes then we need to start the process from the beginning.


## Note

- Initially I was running the spark job by providing jar files at the runtime which was inefficient, So I provided jar files at the time of docker compose. At the runtime I am just referencing the jar file location from docker root folders.

## Commands:

1. Run Docker Compose:

```
cd src && docker compose up -d
```

2. Run java kafka producer

```
cd src/utils/java_producers && mvn exec:java -Dexec.mainClass="com.datamasterylab.TransactionProducer"
```

3. Run pyspark job

```
docker exec -it src-spark-master-1 spark-submit \
    --master spark://spark-master:7077 \
    --jars /opt/bitnami/spark/additional-jars/spark-sql-kafka-0-10_2.12-3.5.0.jar,/opt/bitnami/spark/additional-jars/kafka-clients-3.4.1.jar,/opt/bitnami/spark/additional-jars/spark-token-provider-kafka-0-10_2.12-3.5.0.jar,/opt/bitnami/spark/additional-jars/commons-pool2-2.11.1.jar \
    /opt/bitnami/spark/jobs/main.py
```
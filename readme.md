# Big Data Demo Application
Generates messages, puts to queue, pulls and aggregates them by time-window, writes on-disk and in db.  
Content:  
* **Ansible** and **Docker** images -based self-deploy/run script.
* **Hadoop MapReduce** Python test task.
* **Spark Streaming** Java job and test.
* **Kafka** queue and Kafdrop for monitoring.
* **MongoDB** as storage. 
* **Avro** schema for source data on-disk storage (not for kafka by now). 
* **Spring-boot** source data generator.

# Use
```start.sh```  
Installs: ansible, python, java, docker  
Deploys (to local docker): zookeeper, kafka, kafdrop, mongo, hadoop, spark, spring-boot application.  
```stop.sh``` stops everything.  
```docker rm``` should be used to clean things up manually by you.  

# Problems & comments
It's my first self-taught attempt with this stack.  
Logs config are not ideal, ansible swallows up default output.  
Uses win-utils for running unit tests.  
Uses pure Java for Spark jobs.  
Hadoop images are taken from bitnami, have no data locality.  
Has unused (by now) replica-nodes in virtual network.  
Unstable failover.  
Installation leaves traces, so a manual clean up may be needed.  
May have another unknown to me problems.  

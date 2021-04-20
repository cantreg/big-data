# Big Data Demo Application

* **Ansible** and **Docker** images -based self-deployable/runnable app.
* **Hadoop MapReduce** Python task.
* **Spark Streams** Java jobs.
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

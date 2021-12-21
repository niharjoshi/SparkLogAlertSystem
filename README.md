# Log File Monitoring & Alert System

---

## Introduction

In this project we build a log file monitoring system that sends email alerts to the project stakeholders when any **WARN** or **ERROR** logs are produced by our Log File Generator application.

Our entire project code base is written entirely in Scala and the pipeline is created using the following technology stack:

![Alt text](doc/technologies.jpg?raw=true "Technology Stack")

In the later sections, we will take a detailed look at our code + cloud architecture for this project.

The project comprises 3 Git repositories, each containing their own detailed README files with explanations:

- Project Component 1: https://github.com/niharjoshi/LogFileGeneratorDeployment.git
- Project Component 2: https://github.com/niharjoshi/RedisMonitor.git
- **Project Component 3: https://github.com/niharjoshi/SparkLogAlertSystem.git (current)**

---

## Prerequisites, Installation & Deployment

**A YouTube playlist documenting the deployment process can be found here: https://www.youtube.com/playlist?list=PL0k75q4RIbeuLDnClDzYQ0gZvzSnBlZ8Q**

We recommend cloning this repository onto your local machine and running it from the command-line using the interactive build tool **sbt**.

*Note: In order to install sbt, please follow the OS-specific instructions at https://www.scala-sbt.org/1.x/docs/Setup.html.*

To clone the repo use:
```console
git clone https://github.com/niharjoshi/SparkLogAlertSystem.git
```

Navigate to the repo and use the following command to run the unit test cases:
```console
sbt clean test
```

Next, sbt downloads project dependencies and compiles our Scala classes.
To do this, use the following command:
```console
sbt clean compile
```

To run the Spark alert system locally, you will need to start a Kafka broker on localhost.

*Note: In order to install Kafka, please follow the instructions at https://kafka.apache.org/quickstart*

Change the **broker** parameter to **localhost:9092** and the **topic** parameter to your desired topic name in the application configuration file at ```src/main/resources/application.conf``` to **localhost**.

To use a cloud-hosted Kafka broker, follow the same instructions to change your Kafka broker/topic, but do not forget to add your **base64 encoded AWS_ACCESS_KEY and AWS_SECRET_KEY** to the application configuration.

Next, you will require an AWS SNS topic for the alert system send you email notifications.

To create an SNS topic, use:
```console
aws sns create-topic --name example-topic
```

*Note: Do not forget to subscribe to your SNS topic using your email via the AWS Management Console*

Note down the returned Topic ARN and add it to the application configuration file at ```src/main/resources/application.conf``` under **arn**.

To run the Spark alert system, you will also require an Apache Spark cluster with **spark-submit** enabled.

In this project, we have used **AWS EMR** to create a Spark cluster with 3 nodes - 1 master and 2 slaves.

*Note: To create a Spark cluster locally, please refer to https://medium.com/ymedialabs-innovation/apache-spark-on-a-multi-node-cluster-b75967c8cb2b*

### **To run the application over an AWS EMR cluster, follow the steps below:**

First, set up the aws-cli using the configure command so you can use the AWS CLI to spawn your EMR cluster: 
```console
aws configure

Enter the requested details:
AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
Default region name [None]: us-east-2
Default output format [None]: json
```

Next, to create your EMR cluster:
```console
aws emr create-cluster --name "SparkLogAlertSystem" --release-label emr-5.33.0 --applications Name=Spark \
--ec2-attributes KeyName=myKey --instance-type m5.xlarge --instance-count 3 --use-default-roles
```

Make sure you supply an EC2 key pair while creating the cluster so you can SSH into it.

Once the EMR cluster is ready, SSH into the master node using its public DNS:
```console
ssh -i /path/to/key.pem hadoop@your_emr_master_public_dns

Example
ssh -i ~/Downloads/myKey.pem hadoop@ec2-16-178-232-244.us-east-2.compute.amazonaws.com
```

Once inside the master node, run the following commands to install and set up SBT and Java:
```console
curl -s "https://get.sdkman.io" | bash

source "/home/hadoop/.sdkman/bin/sdkman-init.sh"

sdk install java $(sdk list java | grep -o "8\.[0-9]*\.[0-9]*\.hs-adpt" | head -1)

sdk install sbt
```

You can now clone this repository onto the master node:
```console
git clone https://github.com/niharjoshi/SparkLogAlertSystem.git
```

Next, build the JAR package so you can submit the bundle to the Spark shell:
```console
sbt clean compile assembly
```

The generated JAR file will be located at ```target/scala-2.11/sparklogalertsystem_2.11-0.1.jar ```.

Now, you can run the JAR file over the Spark shell using:
```console
spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.8 /home/hadoop/SparkLogAlertSystem/target/scala-2.11/SparkLogAlertSystem-assembly-0.1.jar
```

*Note: Do not forget to pass the Spark Kafka SDK manually through ```--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.8``` in the command for compatibility purposes.*

You should get an email notification:

![Alt text](doc/email-alert.png?raw=true "Email Alert")

---

## Architecture & Flow of Control

**A YouTube playlist documenting the detailed architecture and flow of control can be found here: https://www.youtube.com/playlist?list=PL0k75q4RIbeuLDnClDzYQ0gZvzSnBlZ8Q**

### Note: This repository pertains to **Project Component 3**.

![Alt text](doc/flowchart.jpg?raw=true "Flow of Control")

To explain in brief, the core Redis monitor application is deployed onto a AWS EKS Kubernetes cluster over a deployment of 2 pods (each running one Docker container of the app).

The logs generated by the application are written into an AWS ElastiCache Redis database using a predefined UUID-based key-value schema.

Next, our Akka actor system running on another Kubernetes deployment continuously monitors the Redis DB and looks for newly added logs.

It filters the WARN and ERROR logs our and puts them into a AWS MSK Apache Kafka topic which are then consumed by a AWS EMR Apache Spark cluster with 1 master and 2 slave nodes.

The Spark application batches the consumed logs and subsequently sends email alerts to the project stakeholders.

### **Project Component 3 Design**

When we submit our JAR file to Spark, it starts reading logs from the Kafka topic that we specified in our application configuration.

As it reads the logs, it adds them to a Spark RDD which is then parallelized over the slave nodes in batches.

![Alt text](doc/spark-shell.png?raw=true "Spark Shell")

An email body is constructed for each batch and this email body is submitted to an AWS SNS topic.

The SNS topic automatically triggers an email notification to the stakeholders.

![Alt text](doc/email-alert.png?raw=true "Email Alert")

---

## Checklist

- [x] All tasks completed
- [x] Installation instructions in README
- [x] In-depth documentation
- [x] Successful AWS deployment
- [x] YouTube video
- [x] More than 5 unit tests
- [x] Comments and explanations
- [x] Logging statements
- [x] No hardcoded values
- [x] No var or heap-based variables used
- [x] No for, while or do-while loops used

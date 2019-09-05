# Description:
Demo Java 8 maven project showcasing Spring boot, Spring cloud GCP, Clould Pub/Sub and Cloud Spanner Database, its deployable to gae-standard

#Prerequisites
Install Java 8 JDK
#Building
./mvnw clean install
#Running
java -jar target/spring-boot-gcp-demo-0.0.1-SNAPSHOT.jar
OR import project into Intellij IDE and run the GcpDemoApplication as a Java application
#Testing
List REST endpoints
curl -i http://localhost:8080
List orders
curl -i http://localhost:8080/orders

Post a greeting message to the Cloud PUb/SUB
curl -i -XPOST http://localhost:8080/greet/goodday

#Deploying to google app engine:
./mvnw appengine:deploy -Dapp.deploy.projectId=[your-gcp-proeject-name]




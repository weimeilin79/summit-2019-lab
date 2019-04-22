# summit-2019-lab
2019 lab flight center
Running the demo

kamel run --name=flight-status -d camel-swagger-java -d camel-jackson -d camel-undertow -d mvn:org.apache.activemq:activemq-camel:5.15.9 -d mvn:org.apache.activemq:activemq-client:5.15.9 FlightStatus.java

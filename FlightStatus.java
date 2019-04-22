/** 
kamel run --name=flight-status -d camel-swagger-java -d camel-jackson -d camel-undertow -d mvn:org.apache.activemq:activemq-camel:5.15.9 -d mvn:org.apache.activemq:activemq-client:5.15.9 FlightStatus.java
*/
import java.util.HashMap;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.component.jackson.JacksonDataFormat;

public class FlightStatus extends RouteBuilder {

    String BROKER_URL = "tcp://broker-amq-tcp.amqfornow.svc:61616";
    HashMap<String, Flight> allrecordsFlights = new HashMap<String, Flight>();
   

    @Override
    public void configure() throws Exception {
        
        restConfiguration()
            .component("undertow")
            .apiContextPath("/api-doc")
            .apiProperty("cors", "true")
            .apiProperty("api.title", "Flight Center")
            .apiProperty("api.version", "1.0")
            .port("8080")
            .bindingMode(RestBindingMode.json);

        rest()
            .get("/flight/status/{flightno}")
                .to("direct:status")
            .post("/flight/rebook/{flightno}/{speakername}/{userid}")
                .to("direct:rebook");

        getContext().addComponent("activemq", ActiveMQComponent.activeMQComponent(BROKER_URL));
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat();
        jacksonDataFormat.setUnmarshalType(MyFlight.class);
        setRecords();

        from("direct:status")
            .setBody(method(this, "queryStatus(${headers.flightno})"));

        from("direct:rebook")
            .setBody(method(this, "rebook(${headers.speakername},${headers.flightno})"))
            .log("rebook from ${headers.speakername} ${headers.flightno} to ${body}")
            .marshal(jacksonDataFormat)
            .toD("activemq:topic:${headers.userid}?username=amq&password=password&exchangePattern=InOnly")
            .unmarshal(jacksonDataFormat);
    }

    public Flight queryStatus(String flightNo){
        Flight flight = allrecordsFlights.get(flightNo);
        if(flight==null){
            int hour = (int)Math.floor(Math.random() * 24);
            int min = (int)Math.floor(Math.random() * 59);
            flight = genFlight(flightNo,"On-Time", Integer.valueOf(hour)+":"+Integer.valueOf(min)); 
        }
        allrecordsFlights.put(flight.getFlightno(), flight);
        return flight;
    }

    public MyFlight rebook(String speakername, String flightNo){
        Flight flight = queryStatus(flightNo);
        flightNo = flight.getFlightno().substring(0, 2) + ((int)Math.floor(Math.random() * 9999));
        int oldHour = Integer.valueOf(flight.getArrivaltime().substring(0,flight.getArrivaltime().indexOf(":")));
        int newhour = ((int)Math.floor(Math.random() * 24-oldHour)) + oldHour;
        int min = (int)Math.floor(Math.random() * 59);
        
        Flight newFlight = genFlight(flightNo,"On-Time", newhour+":"+min);
        allrecordsFlights.put(newFlight.getFlightno(), newFlight);
        MyFlight myFlight = new MyFlight();
        myFlight.setSpeakerName(speakername);
        myFlight.setFlight(newFlight);
        return myFlight;
    }
    

     
    public void setRecords(){
        allrecordsFlights.clear();
        allrecordsFlights.put("CA1810",genFlight("CA1810","On-Time", "9:30"));
        allrecordsFlights.put("DL3929",genFlight("DL3929","Delayed", "9:45"));                 
        allrecordsFlights.put("VS3688",genFlight("VS3688","On-Time", "10:25"));
        allrecordsFlights.put("AM2324",genFlight("AM2324","On-Time", "11:30"));
        allrecordsFlights.put("JL7455",genFlight("JL7455","Delayed", "12:45"));
        allrecordsFlights.put("B6111 ",genFlight("B61112","On-Time", "12:55"));
        allrecordsFlights.put("HA2441",genFlight("HA2441","On-Time", "13:05"));
        allrecordsFlights.put("AA2623",genFlight("AA2623","Delayed", "13:30")); 
        allrecordsFlights.put("DL0677",genFlight("DL0677","On-Time", "13:50"));
        allrecordsFlights.put("UA 45 ",genFlight("UA0045","Delayed", "14:15"));
        allrecordsFlights.put("TK1993",genFlight("TK1993","On-Time", "15:30"));
        allrecordsFlights.put("LH345 ",genFlight("LH3452","On-Time", "15:45"));
        allrecordsFlights.put("BS2945",genFlight("BS2945","Delayed", "15:50")); 
        allrecordsFlights.put("JJ324 ",genFlight("JJ3247","Delayed", "18:00"));

    }
    
    private Flight genFlight(String flightno, String status,String arrivaltime){
        Flight flight = new Flight();
            flight.setFlightno(flightno);
            flight.setStatus(status);
            flight.setArrivaltime(arrivaltime);
        return flight;
    }


    private static class MyFlight implements java.io.Serializable{
        private static final long serialVersionUID = 1L;
        
        Flight flight;
        String speakerName;

        public void setFlight(Flight flight){
            this.flight = flight;
        }
        public void setSpeakerName(String speakerName){
            this.speakerName = speakerName;
        }

        public Flight getFlight(){
            return flight;
        }
        public String getSpeakerName(){
            return speakerName;
        }
        
        @Override
        public String toString(){
            return "speakerName:" + speakerName + " Flightno:"+ flight.getFlightno() + " Arrivaltime:"+ flight.getArrivaltime() + " Status:"+ flight.getStatus();
        }
    }

    private static class Flight implements java.io.Serializable{
        private static final long serialVersionUID = 1L;
        String flightno;
        String status;
        String arrivaltime;

        public String getFlightno(){
            return flightno;
        }
        public String getStatus(){
            return status;
        }
        public String getArrivaltime(){
            return arrivaltime;
        }

        public void setFlightno(String flightno){
            this.flightno= flightno;
        }

        public void setStatus(String status){
            this.status= status;
        }

        public void setArrivaltime(String arrivaltime){
            this.arrivaltime= arrivaltime;
        }
    }

    
}
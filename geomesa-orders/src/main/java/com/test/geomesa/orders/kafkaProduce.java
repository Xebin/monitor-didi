package com.test.geomesa.orders;

import org.joda.time.Duration;
import org.joda.time.Instant;

import utils.mesaUtils;


public class kafkaProduce {
    public static void main(String[] args) throws Exception {


        // create the schema which creates a topic in Kafka
        // (only needs to be done once)
        final String sftName = "kafkaOrders";
        final String sftSchema = "orderId:String,driverId:String,userId:String,ts:long,update:long,*geom:Point:srid=4326";
       
//
//    	for(int i=0;i<10;i++){
    		 System.out.println("------------------------start produce...:");
    	        mesaUtils.liveProduce(sftSchema, sftName);    
//    	        Thread.sleep(5*1000);
//    	}    	        
    	//System.exit(0);
    }
}




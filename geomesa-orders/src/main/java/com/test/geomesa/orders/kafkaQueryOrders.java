package com.test.geomesa.orders;

import org.apache.commons.cli.*;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.locationtech.geomesa.kafka.KafkaDataStoreHelper;
import org.locationtech.geomesa.kafka.ReplayConfig;
import org.locationtech.geomesa.kafka.ReplayTimeHelper;
import org.locationtech.geomesa.utils.geotools.SimpleFeatureTypes;
import org.locationtech.geomesa.utils.text.WKTUtils$;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import utils.mesaUtils;
import utils.timeUtils;

import java.io.IOException;
import java.util.*;

public class kafkaQueryOrders {
    public static void main(String[] args) throws Exception {

        // create the schema which creates a topic in Kafka
        // (only needs to be done once)
        final String sftName = "kafkaOrders";
        final String sftSchema = "orderId:String,driverId:String,userId:String,ts:long,update:long,*geom:Point:srid=4326";
        Instant replayStart=new Instant().minus(3600*1000);
        System.out.println("------------------------start repquery:"+replayStart.getMillis());
//        mesaUtils.liveProduce(sftSchema, sftName);
       // System.out.println("------------------------end:"+replayEnd.getMillis());

//        while(true){
//            mesaUtils.liveAllQuery(sftSchema, sftName);
//        }
       
       
       
       Duration readBehind = new Duration(24*3600*1000); // 1 second readBehind
        Instant replayEnd=new Instant();
        mesaUtils.repQuery(replayStart, replayEnd, replayEnd, readBehind, sftSchema, sftName);
        
        
       //    System.out.println("----------replay query::"+String.valueOf(i));
        //   Thread.sleep(6*1000);
    //   }
       
      // System.exit(0);
    }
}




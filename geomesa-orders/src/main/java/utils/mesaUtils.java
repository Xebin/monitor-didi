package utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.locationtech.geomesa.kafka.KafkaDataStoreFactory;
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

public class mesaUtils {
	
	public static String brokers = "localhost:9092";
	public static  String zookeepers = "localhost:2181";
	public static String zkPath =  "/geomesa/ds/kafka";

	// build parameters map\
	public static Map<String, Serializable> params = new HashMap<String, Serializable>();
	public static KafkaDataStoreFactory factory = new KafkaDataStoreFactory();
	public static DataStore consumerDS=null;
	public static DataStore producerDS=null;
	public static  SimpleFeatureSource consumerFS=null;
	public static SimpleFeatureStore producerFS=null;

	static{
			//Map<String, Serializable> params = new HashMap<>();
			params.put("brokers",brokers);
			params.put("zookeepers", zookeepers);
			
			// optional
			//params.put("zkPath", zkPath);
			params.put("isProducer", "false");
			 consumerDS = factory.createDataStore(params);
			params.put("isProducer", "true");
			 producerDS = factory.createDataStore(params);
	}
	
	public static DataStore getConsumerDS() throws IOException{
		params.put("isProducer", "false");
        DataStore consumerDS = DataStoreFinder.getDataStore(params);
		return consumerDS;
	}
	
	public static DataStore getProducerDS(){
		return producerDS;
	}
	public static void init(String sftSchema ,String sftName) throws IOException, InterruptedException{
		SimpleFeatureType sft = SimpleFeatureTypes.createType(sftName, sftSchema);
        SimpleFeatureType preppedOutputSft = KafkaDataStoreHelper.createStreamingSFT(sft, zkPath);
        
        // only create the schema if it hasn't been created already
        if (!Arrays.asList(producerDS.getTypeNames()).contains(sftName))
            producerDS.createSchema(preppedOutputSft);
	       producerFS = (SimpleFeatureStore) producerDS.getFeatureSource(sftName);
        // the live consumer must be created before the producer writes features
        // in order to read streaming data.
        // i.e. the live consumer will only read data written after its instantiation
        consumerFS = consumerDS.getFeatureSource(sftName);
	}
public static void liveProduce(String sftSchema ,String sftName) throws IOException, InterruptedException{
		init(sftSchema , sftName);
		SimpleFeatureType sft = SimpleFeatureTypes.createType(sftName, sftSchema);

	      // creates and adds SimpleFeatures to the producer every 1/5th of a second
	      long t1=timeUtils.getUtc();
	      System.out.println("start Writing features to Kafka...ts: "+String.valueOf(t1));
	     addSimpleFeatures(sft, producerFS);
	     long t2=timeUtils.getUtc();
	     System.out.println("finished Writing features to Kafkats: "+String.valueOf(t2)+"... duration:"+String.valueOf(t2-t1));
	}
	
	public static SimpleFeatureCollection liveFilterQuery(Filter filter,String sftSchema ,String sftName) throws IOException{
        // read from Kafka after writing all the features.
        // LIVE CONSUMER - will obtain the current state of SimpleFeatures
        System.out.println("\nConsuming with the live consumer...");
        long t3=timeUtils.getUtc();
        System.out.println("start query filter...ts: "+String.valueOf(t3));
        SimpleFeatureCollection featureCollection = consumerFS.getFeatures(filter);
        long t4=timeUtils.getUtc();
        System.out.println("end query filter... ts: "+String.valueOf(t4)+"--duration:"+String.valueOf(t4-t3));
        System.out.println(featureCollection.size() + " filter features were written to Kafka");
        return featureCollection;
	}
	
	public static SimpleFeatureCollection liveAllQuery(String sftSchema ,String sftName) throws IOException{
        // read from Kafka after writing all the features.
        // LIVE CONSUMER - will obtain the current state of SimpleFeatures
        System.out.println("\nConsuming with the live consumer...");
        long t3=timeUtils.getUtc();
        System.out.println("start query all...ts: "+String.valueOf(t3));
        SimpleFeatureCollection featureCollection = consumerFS.getFeatures();

        long t4=timeUtils.getUtc();
        System.out.println("end query all... ts: "+String.valueOf(t4)+"--duration:"+String.valueOf(t4-t3));
//        System.out.println(" all features num:"+featureCollection.size() );
        return featureCollection;
	}
	
	 // add a SimpleFeature to the producer every half second
    @SuppressWarnings("unchecked")
	public static void addSimpleFeatures(SimpleFeatureType sft,FeatureStore producerFS)
            throws InterruptedException, IOException {
        final int MIN_X = -180;
        final int MAX_X = 180;
        final int MIN_Y = -90;
        final int MAX_Y = 90;
        final int DX = 2;
        final int DY = 1;
        final Random random = new Random();

        // creates and updates two SimpleFeatures.
        // the first time this for loop runs the two SimpleFeatures are created.
        // in the subsequent iterations of the for loop, the two SimpleFeatures are updated.
        int numFeatures = (MAX_X - MIN_X) / DX;

       // final DateTime MIN_DATE = new DateTime(2015, 1, 1, 0, 0, 0, DateTimeZone.forID("UTC"));
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(sft);
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        int n=0;
        //input test data
        //sftSchema = "orderId:String,driverId:String,userId:String,ts:long,update:long,longitude:float,latitude:float";
        for (int j= 1; j <= 10; j++) {
        	 for (int i = 1; i <= 100; i++) {
		        	builder.add("order0"+String.valueOf(i));
		        	builder.add("driver0"+String.valueOf(i));
		        	builder.add("user0"+String.valueOf(i));
		        	builder.add(1462347417+n*100);
		        	builder.add(timeUtils.getUtc());
		        	builder.add(WKTUtils$.MODULE$.read("POINT(" + (MIN_X + DX * i) + " " + (MAX_Y - DY * i) + ")"));
		            SimpleFeature feature1 = builder.buildFeature(String.valueOf(2000+n++));//note:unique id ,
		            
		            // write the SimpleFeatures to Kafka
		            featureCollection.add(feature1);
//		            featureCollection.add(feature2);
		            producerFS.addFeatures(featureCollection);
		            featureCollection.clear();
		
		            // wait 100 ms in between updating SimpleFeatures to simulate a stream of data
		            Thread.sleep(1000);
//		            System.out.println("add kafka feature..."+String.valueOf(n));
        	 }
        }
        
        //clear teatures
        producerFS.removeFeatures(Filter.INCLUDE);
    }

    //replay consumer
    public static SimpleFeatureCollection repQuery( Instant replayStart,Instant replayEnd,Instant historicalTime,Duration readBehind,String sftSchema ,String sftName) throws IOException, InterruptedException{
    	   // consumerFS = consumerDS.getFeatureSource(sftName);

	    	ReplayConfig replayConfig = new ReplayConfig(replayStart, replayEnd, readBehind);
	    	SimpleFeatureType sft = consumerDS.getSchema(sftName);
	    	
	        SimpleFeatureType streamingSFT = KafkaDataStoreHelper.createStreamingSFT(sft, zkPath);
	
	    	SimpleFeatureType replaySFT = KafkaDataStoreHelper.createReplaySFT(streamingSFT, replayConfig);
	    	consumerDS.createSchema(replaySFT);
	
	    	String replayTimeName = replaySFT.getTypeName();
	    	SimpleFeatureSource replayFeatureSource = consumerDS.getFeatureSource(replayTimeName);
	    	FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
	
			Filter timeFilter = ReplayTimeHelper.toFilter(historicalTime);
			
			SimpleFeatureCollection featureCollection =replayFeatureSource.getFeatures(timeFilter);
	        System.out.println(featureCollection.size() + " features were repquery");
	        
	        //clear teatures
	        //producerFS.removeFeatures(Filter.INCLUDE);
	        
			return featureCollection;

    }  
    
    
    // prints out attribute values for a SimpleFeature
    public static void printFeature(SimpleFeature f) {
        Iterator<Property> props = f.getProperties().iterator();
        int propCount = f.getAttributeCount();
        System.out.print("fid:" + f.getID());
        for (int i = 0; i < propCount; i++) {
            Name propName = props.next().getName();
            System.out.print(" | " + propName + ":" + f.getAttribute(propName));
        }
        System.out.println();
    }
}

package com.test.geomesa.orders;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.locationtech.geomesa.kafka.KafkaFeatureEvent;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

public class kafkaOrdersListener {

    private static void registerListeners(DataStore consumerDS,String sftName) throws IOException, SchemaException {
            registerListenerForFeature(consumerDS, sftName);
    }

    // the live consumer must be created before the producer writes features
    // in order to read streaming data.
    // i.e. the live consumer will only read data written after its instantiation
    private static void registerListenerForFeature(DataStore consumerDS, final String sftName) throws IOException, SchemaException {
    	
        SimpleFeatureSource consumerFS = consumerDS.getFeatureSource(sftName);
        System.out.println("Registering a feature listener for type " + sftName + ".");
        final DataStore accumuloDS=utils.accumuloUtils.getDataStore();
        final String simpleFeatureTypeName = "ordersTest";
        final SimpleFeatureType simpleFeatureType = utils.accumuloUtils.createSimpleFeatureType(simpleFeatureTypeName);
   		final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

        consumerFS.addFeatureListener(new FeatureListener() {
            public void changed(FeatureEvent featureEvent) {
                System.out.println("Received FeatureEvent from layer " + sftName + " of Type: " + featureEvent.getType());

                if (featureEvent.getType() == FeatureEvent.Type.CHANGED &&
                        featureEvent instanceof KafkaFeatureEvent) {
                	    SimpleFeature feature=((KafkaFeatureEvent) featureEvent).feature();
                	    System.out.println("listen..");
                	    utils.mesaUtils.printFeature(feature);

                	    SimpleFeature accumuloSF =dto.kaf2Accumulo.kaf2Accumulo(feature,simpleFeatureType);
	               		featureCollection.add(accumuloSF);
	               		
	               		try {
			               		if(featureCollection.getCount()>3){	
											utils.accumuloUtils.insertFeatures(simpleFeatureTypeName, accumuloDS, featureCollection);
						                    System.out.println("insert accumulo suc.... " );
											featureCollection.clear();
			                       }else{
					                    System.out.println("collecting accumulo feature .... " );
			                       }
			                } catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
			                    System.out.println("erro insert accumulo " );
						}
                }
                if (featureEvent.getType() == FeatureEvent.Type.REMOVED) {
                    System.out.println("Received Delete for filter: " + featureEvent.getFilter());
                }
            }
        });
    }
    

    
    
    
    public static void main(String[] args) throws Exception {

        DataStore consumerDS = utils.mesaUtils.getConsumerDS();

        // verify that we got back our KafkaDataStore object properly
        if (consumerDS == null) {
            throw new Exception("Null consumer KafkaDataStore");
        }

        // create the schema which creates a topic in Kafka
        // (only needs to be done once)
        registerListeners(consumerDS,"kafkaOrders");

        while (true) {
            // Wait for user to terminate with ctrl-C.
        }
    }
}

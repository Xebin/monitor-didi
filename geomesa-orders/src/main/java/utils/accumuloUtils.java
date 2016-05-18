package utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.locationtech.geomesa.accumulo.index.Constants;
import org.locationtech.geomesa.kafka.KafkaDataStoreFactory;
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.locationtech.geomesa.utils.interop.WKTUtils;
import org.locationtech.geomesa.utils.text.WKTUtils$;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Geometry;

public class accumuloUtils {
	public static String brokers = "localhost:9092";
	public static  String zookeepers = "localhost:2181";
	public static Map<String, String> params = new HashMap<String, String>();
	//public static DataStore ds=null;
	static{		
			params.put("instanceId", "xebin");
			params.put("zookeepers", zookeepers);
			params.put("user", "root");
			params.put("password", "20141211");
			//params.put("tableName", "ordersTest");
			params.put("auths", "");
	}
	
	  public static Map<String,String> sch_map=new  HashMap<String, String>();
	  static{
		  sch_map.put("orders11Test", instances.sftSchema.test_sch);
		  sch_map.put("ordersTest", instances.sftSchema.test_sch);
		  sch_map.put("hitch_orders", instances.sftSchema.hitch_sch);
		  sch_map.put("special_cars_orders", instances.sftSchema.special_sch);
		  sch_map.put("taxis_orders", instances.sftSchema.taxi_sch);
	  }

	
//	private static DataStore getDataStore() throws IOException, SchemaException{
//		return ds;
//	}
	
	public static DataStore getDataStore(String tableName) throws IOException, SchemaException{
		params.put("tableName", tableName);
		DataStore ds=DataStoreFinder.getDataStore(params);
		
        SimpleFeatureType simpleFeatureType = createSimpleFeatureType(tableName);

        // write Feature-specific metadata to the destination table in Accumulo
        // (first creating the table if it does not already exist); you only need
        // to create the FeatureType schema the *first* time you write any Features
        // of this type to the table
        ds.createSchema(simpleFeatureType);
		
		return ds;
	}
	
	//  sftSchema = "orderId:String,driverId:String,userId:String,ts:long,update:long,*geom:Point:srid=4326"
	public  static SimpleFeatureType createSimpleFeatureType(String simpleFeatureTypeName)
            throws SchemaException {
        SimpleFeatureType simpleFeatureType =
                SimpleFeatureTypes.createType(simpleFeatureTypeName, sch_map.get(simpleFeatureTypeName));
        return simpleFeatureType;
    }

	
	public  static void insertFeatures(String simpleFeatureTypeName,
                               DataStore dataStore,
                               FeatureCollection featureCollection)
            throws IOException {

        FeatureStore featureStore = (SimpleFeatureStore) dataStore.getFeatureSource(simpleFeatureTypeName);
        featureStore.addFeatures(featureCollection);
    }

	

	public  static void secondaryIndexExample(String simpleFeatureTypeName,
                                      DataStore dataStore,
                                      String[] attributeFields,
                                      String attributesQuery,
                                      int maxFeatures,
                                      String sortByField)
            throws CQLException, IOException {

        // construct a (E)CQL filter from the search parameters,
        // and use that as the basis for the query
        Filter cqlFilter = CQL.toFilter(attributesQuery);
        Query query = new Query(simpleFeatureTypeName, cqlFilter);

        query.setPropertyNames(attributeFields);
        query.setMaxFeatures(maxFeatures);

        if (!sortByField.equals("")) {
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
            SortBy[] sort = new SortBy[]{ff.sort(sortByField, SortOrder.DESCENDING)};
            query.setSortBy(sort);
        }

        // submit the query, and get back an iterator over matching features
        @SuppressWarnings("rawtypes")
		FeatureSource featureSource = dataStore.getFeatureSource(simpleFeatureTypeName);
        @SuppressWarnings("rawtypes")
		FeatureIterator featureItr = featureSource.getFeatures(query).features();

        // loop through all results
        int n = 0;
        while (featureItr.hasNext()) {
            Feature feature = featureItr.next();
            StringBuilder sb = new StringBuilder();
            sb.append("Feature ID ").append(feature.getIdentifier());

            for (String field : attributeFields) {
                sb.append(" | ").append(field).append(": ").append(feature.getProperty(field).getValue());
            }
            System.out.println(sb.toString());
            n++;
        }
        System.out.println("feartures count:"+String.valueOf(n));
        featureItr.close();
    }
	
}

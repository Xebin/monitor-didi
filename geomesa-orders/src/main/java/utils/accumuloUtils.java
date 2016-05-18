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
	public static String zkPath =  "/geomesa/ds/kafka";

	// build parameters map\
	public static Map<String, String> params = new HashMap<String, String>();
	public static DataStore ds=null;

	static{		
			params.put("instanceId", "xebin");
			params.put("zookeepers", zookeepers);
			params.put("user", "root");
			params.put("password", "20141211");
			params.put("tableName", "ordersTest");
			params.put("auths", "");
			try {
				ds=DataStoreFinder.getDataStore(params);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static DataStore getDataStore() throws IOException, SchemaException{
//        String simpleFeatureTypeName = "ordersTest1";
//        SimpleFeatureType simpleFeatureType = createSimpleFeatureType(simpleFeatureTypeName);
//        ds.createSchema(simpleFeatureType);

		return ds;
	}
	
	//  sftSchema = "orderId:String,driverId:String,userId:String,ts:long,update:long,*geom:Point:srid=4326"
	public  static SimpleFeatureType createSimpleFeatureType(String simpleFeatureTypeName)
            throws SchemaException {

        // list the attributes that constitute the feature type
        List<String> attributes = Lists.newArrayList(
                "orderId:String:index=full",
                "driverId:String",
                "userId:String",
                "ts:java.lang.Long",     // some types require full qualification (see DataUtilities docs)
                "update:java.lang.Long",               // a date-time field is optional, but can be indexed
                "*geom:Point:srid=4326"  // the "*" denotes the default geometry (used for indexing)
        );

        // create the bare simple-feature type
        String simpleFeatureTypeSchema = Joiner.on(",").join(attributes);
        SimpleFeatureType simpleFeatureType =
                SimpleFeatureTypes.createType(simpleFeatureTypeName, simpleFeatureTypeSchema);

        // use the user-data (hints) to specify which date-time field is meant to be indexed;
        // if you skip this step, your data will still be stored, it simply won't be indexed
        simpleFeatureType.getUserData().put(Constants.SF_PROPERTY_START_TIME, "update");

        return simpleFeatureType;
    }

	public    static FeatureCollection createNewFeatures(SimpleFeatureType simpleFeatureType, int numNewFeatures) {
		
		DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

        String id;
        Object[] NO_VALUES = {};
        String[] PEOPLE_NAMES = {"Addams", "Bierce", "Clemens"};
        Long SECONDS_PER_YEAR = 365L * 24L * 60L * 60L;
        Random random = new Random(5771);
        DateTime MIN_DATE = new DateTime(2014, 1, 1, 0, 0, 0, DateTimeZone.forID("UTC"));
        Double MIN_X = -78.0;
        Double MIN_Y = -39.0;
        Double DX = 2.0;
        Double DY = 2.0;

        for (int i = 0; i < 10; i ++) {
            // create the new (unique) identifier and empty feature shell
            id = "Observation." + Integer.toString(i);
            SimpleFeature simpleFeature = SimpleFeatureBuilder.build(simpleFeatureType, NO_VALUES, id);

            // be sure to tell GeoTools explicitly that you want to use the ID you provided
            simpleFeature.getUserData().put(Hints.USE_PROVIDED_FID, java.lang.Boolean.TRUE);

            // populate the new feature's attributes
            simpleFeature.setAttribute("orderId", "orderId"+String.valueOf(i));
            simpleFeature.setAttribute("driverId", "driverId"+String.valueOf(i));
            simpleFeature.setAttribute("userId", "userId"+String.valueOf(i));
            simpleFeature.setAttribute("ts", timeUtils.getUtc());

            // location:  construct a random point within a 2-degree-per-side square
            double x = MIN_X + random.nextDouble() * DX;
            double y = MIN_Y + random.nextDouble() * DY;
            Geometry geometry = WKTUtils.read("POINT(" + x + " " + y + ")");
            // date-time:  construct a random instant within a year
            simpleFeature.setAttribute("geom", geometry);
           // DateTime dateTime = MIN_DATE.plusSeconds((int) Math.round(random.nextDouble() * SECONDS_PER_YEAR));
            simpleFeature.setAttribute("update", timeUtils.getUtc());

            // accumulate this new feature in the collection
            featureCollection.add(simpleFeature);
        }

        return featureCollection;
    }

	public  static void insertFeatures(String simpleFeatureTypeName,
                               DataStore dataStore,
                               FeatureCollection featureCollection)
            throws IOException {

        FeatureStore featureStore = (SimpleFeatureStore) dataStore.getFeatureSource(simpleFeatureTypeName);
        featureStore.addFeatures(featureCollection);
    }

	public  static Filter createFilter(String geomField, double x0, double y0, double x1, double y1,
                               String dateField, String t0, String t1,
                               String attributesQuery)
            throws CQLException, IOException {

        // there are many different geometric predicates that might be used;
        // here, we just use a bounding-box (BBOX) predicate as an example.
        // this is useful for a rectangular query area
        String cqlGeometry = "BBOX(" + geomField + ", " +
                x0 + ", " + y0 + ", " + x1 + ", " + y1 + ")";

        // there are also quite a few temporal predicates; here, we use a
        // "DURING" predicate, because we have a fixed range of times that
        // we want to query
        String cqlDates = "(" + dateField + " DURING " + t0 + "/" + t1 + ")";

        // there are quite a few predicates that can operate on other attribute
        // types; the GeoTools Filter constant "INCLUDE" is a default that means
        // to accept everything
        String cqlAttributes = attributesQuery == null ? "INCLUDE" : attributesQuery;

        String cql = cqlGeometry + " AND " + cqlDates  + " AND " + cqlAttributes;
        return CQL.toFilter(cql);
    }

	public  static void queryFeatures(String simpleFeatureTypeName,
                              DataStore dataStore,
                              String geomField, double x0, double y0, double x1, double y1,
                              String dateField, String t0, String t1,
                              String attributesQuery)
            throws CQLException, IOException {

        // construct a (E)CQL filter from the search parameters,
        // and use that as the basis for the query
        Filter cqlFilter = createFilter(geomField, x0, y0, x1, y1, dateField, t0, t1, attributesQuery);
        Query query = new Query(simpleFeatureTypeName, cqlFilter);

        // submit the query, and get back an iterator over matching features
        @SuppressWarnings("rawtypes")
		FeatureSource featureSource = dataStore.getFeatureSource(simpleFeatureTypeName);
        @SuppressWarnings("rawtypes")
		FeatureIterator featureItr = featureSource.getFeatures(query).features();//query

        // loop through all results
        int n = 0;
        while (featureItr.hasNext()) {
            Feature feature = featureItr.next();
            System.out.println((++n) + ".  " +
                    feature.getProperty("Who").getValue() + "|" +
                    feature.getProperty("What").getValue() + "|" +
                    feature.getProperty("When").getValue() + "|" +
                    feature.getProperty("Where").getValue() + "|" +
                    feature.getProperty("Why").getValue());
        }
        featureItr.close();
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


	public  static void queryByCql(String simpleFeatureTypeName,
            DataStore dataStore,
            Filter cqlFilter)
throws CQLException, IOException {

		// construct a (E)CQL filter from the search parameters,
		// and use that as the basis for the query
	//	Filter cqlFilter = CQL.toFilter(attributesQuery);
		
		
		// submit the query, and get back an iterator over matching features
		FeatureSource featureSource = dataStore.getFeatureSource(simpleFeatureTypeName);
		//FeatureCollection fc= featureSource.getFeatures(cqlFilter);
		FeatureIterator featureItr =featureSource.getFeatures().features();
		
		// loop through all results
		int n = 0;
		while (featureItr.hasNext()) {
			//Feature feature = featureItr.next();
			n++;
		}
		System.out.println("feartures count:"+String.valueOf(n));
		featureItr.close();
}
	
}

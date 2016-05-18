package dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.feature.SchemaException;
import org.locationtech.geomesa.accumulo.index.Constants;
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.opengis.feature.simple.SimpleFeatureType;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;

public class accUtils {
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
	
	public static DataStore getDataStore() throws IOException, SchemaException{
        String simpleFeatureTypeName = "ordersTest";
        SimpleFeatureType simpleFeatureType = createSimpleFeatureType(simpleFeatureTypeName);

        // write Feature-specific metadata to the destination table in Accumulo
        // (first creating the table if it does not already exist); you only need
        // to create the FeatureType schema the *first* time you write any Features
        // of this type to the table
        ds.createSchema(simpleFeatureType);
		return ds;
	}
}

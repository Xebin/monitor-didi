package dto;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class kaf2Accumulo {
        public static SimpleFeature kaf2Accumulo(SimpleFeature sf,SimpleFeatureType simpleFeatureType){
        	
	        	String id=sf.getID();
	            Object[] NO_VALUES = {};
                 SimpleFeature simpleFeature = SimpleFeatureBuilder.build(simpleFeatureType, NO_VALUES, id);
        	     for( Property p:sf.getProperties()){
        	    	    simpleFeature.setAttribute(p.getName(), p.getValue());
        	     }
        	     
        	     return simpleFeature;
        }
}

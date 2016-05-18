/*
 * Copyright (c) 2013-2016 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 */

package com.test.geomesa.orders;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Geometry;

import org.apache.commons.cli.*;
import org.geotools.data.*;
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
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.locationtech.geomesa.utils.interop.WKTUtils;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AccumuloQueryOrders {
    static final String INSTANCE_ID = "instanceId";
    static final String ZOOKEEPERS = "zookeepers";
    static final String USER = "user";
    static final String PASSWORD = "password";
    static final String AUTHS = "auths";
    static final String TABLE_NAME = "tableName";

    public static void main(String[] args) throws Exception {
    	String simpleFeatureTypeName="ordersTest";

      long t5=utils.timeUtils.getUtc();
      System.out.println("start query filter... :"+String.valueOf(t5));
      String p1="(0 0,0 35,25 70,50 70,75 35,50 0,25 35,0 0)";
      String p2="(0 0,10 0,25 35,25 20,50 20,75 35,50 70,25 70,0 35,10 20,0 0)";
      String p3="(0 0,0 70,25 85,50 110,80 0,0 0)";
      String p4="(0 0,10 40,20 40,10 20,10 10,30 10,30 20,50 20,50 10,70 10,70 60,60 80,50 60,40 80,30 60,20 80,0 80, 0 0)";
      String p5="(0 0,10 70,60 0,0 0)";
      
       Filter filter=CQL.toFilter("INTERSECTS(geom, POLYGON ("+p5+"))");
       
        // query a few Features from this table
//        System.out.println("Submitting query");
//        queryFeatures(simpleFeatureTypeName, dataStore,
//                "Where", -77.5, -37.5, -76.5, -36.5,
//                "When", "2014-07-01T00:00:00.000Z", "2014-09-30T23:59:59.999Z",
//                "(Who = 'Bierce')");
    	DataStore dataStore=utils.accumuloUtils.getDataStore("ordersTest");
    	
//    	utils.accumuloUtils.queryByCql(simpleFeatureTypeName, dataStore, filter);
    	
    	
        System.out.println("Submitting secondary index query");
        utils.accumuloUtils.secondaryIndexExample("ordersTest", dataStore,
                new String[]{"driverId"},
                "(driverId = 'driver021')",
                5,
                "");
        System.out.println("Submitting secondary index query with sorting (sorted by 'What' descending)");
        utils.accumuloUtils.secondaryIndexExample(simpleFeatureTypeName, dataStore,
        	      new String[]{"driverId"},
                  "(driverId = 'driver020')",
                5,
                "driverId");
    }
}

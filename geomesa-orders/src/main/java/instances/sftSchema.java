package instances;

public class sftSchema {
		public final static String hitch_sch="timestamp:Date,db:String,table:String, optype:String,    id:long,    order_id:long,    passenger_id:long,   "
				+ " driver_id:long,    route_id:int,    session_id:long,    f_status:int,    t_status:int,    status:int,    channel:int,    version:String,    from_area_id:int,  "
				+ "  to_area_id:int,    serial:long,    from_location:Point:srid=4326,    from_name:String,    from_address:String,    to_llocation:Point:srid=4326,    "
				+ "to_name:String,    to_address:String,    extra_info:String,    passenger_sum_num:int,    pay_price:float,    expense_detail:String,    driver_award_num:int,   "
				+ " pay_id:String,    create_time:Date,    setup_time:Date,    strive_time:Date,    strive_location:Point:srid=4326,prepay_time:Date,    arrive_time:Date,  "
				+ "  pay_time:Date,    cancel_time:Date,    navi_distance:int,    cancel_reason_type:int,    cancel_reason_ext:String,    driver_complain:String,  "
				+ "  passenger_complain:String,    hongbao_url:String,    hongbao_cnt:int,    hongbao_id:String,    driver_hongbao_id:String,    os_type:int,    flag:int,   "
				+ " business_area:String,    passenger_num:int,    is_o2o:int,    setup_desc:int,   /driver_version:String,    is_carpool:int,    carpool_id:long,    strive_source:int,   "
				+ " reach_time:Date,    reach_location:Point:srid=4326,    free_type:int,    pay_status:int,    complain_status:float,    final_complain_status:int";
		
		public final static String special_sch="timestamp:Date,    db:String,    table:String,    optype:String,    order_id:long,    driver_id:long,    driver_phone:String"
			   +" passenger_id:long,    passenger_phone:String,    travel_id:int,    passenger_count:int,    schema_id:int,    combo_type:int,    combo_id:int,    strategy_token:String,"
			   +" car_id:long,    area:int,    district:String,    type:String,    extra_type:long,    driver_type:int,    input:int,    require_level:int,    strive_car_level:int,"
			   + "    current_location:Point:srid=4326,    starting_name:String    starting_location:Point:srid=4326,    dest_name:String,    dest_location:Point:srid=4326,"
			   + "    driver_start_distance:int,   start_dest_distance:int,    departure_time:Date,    strive_time:Date,    consult_time:Date,    arrive_time:Date,    setoncar_time:Date,"
			   + "    f_begin_charge_time:Date,    t_begin_charge_time:Date,    begin_charge_time:Date,    finish_time:Date,    delay_time_start:Date,    tip:int,    product_id:int,"
			   + "    call_times:int,    pre_total_fee:float,    cap_price:float,    channel:int,    f_order_status:int,    t_order_status:int,    order_status:int,    consult_status:int,"
			   + "    consult_min:int,    is_pay:int,    pay_type:int,    close_reason:0,    resend_reason:,    version:3,    bouns:0,    extra_info:String,    remark:String,"
			   + "    _birth_time:Date    _create_time:Date,    f__modify_time:Date,    t__modify_time:Date,    _modify_time:Date,    _status:int,    dynamic_price:int,"
			   + "    driver_display_price:float,    is_platform_paid:int,    assign_type:int,    complete_type:int,    source_type:int";
		
		public final static String taxi_sch=" timestamp:Date,			    db:String,			    table:String,			    optype:String,			    orderId:long,			    trip_id:int,"
				+ "	    passengerId:long,			    token:int,			    driverId:long,			    f_status:int,			    t_status:int,			    status:int,			    type:int,"
				+ "			    location:Point:srid=4326,			    address:String,			    destination:String,			    setuptime:Date,			    tip:int,			    exp:int,"
				+ "			    waittime:long,			    callCount:int,			    distance:int,			    length:int,			    verifyMessage:String			    createTime:Date,"
				+ "			    striveTime:Date,			    f_arriveTime:Date,			    t_arriveTime:Date			    arriveTime:Date,			    getofftime:Date,"
				+ "			    aboardTime:Date,			    cancelTime:Date,			    pCommentGread:int,			    pCommentText:String,			    pCommentTime:Date,"
				+ "			    pCommentStatus:int,			    dCommentGread:int,			    dCommentText:String,			    dCommentTime:Date,			    dCommentStatus:int,"
				+ "			    channel:int,			    area:int,			    version:int,			    remark:String,			    bonus:int,			    voicelength:int,			    voiceTime:int,"
				+ "			    extra_info:Srting,			    pay_info:String,			    dest_location:Point:srid=4326,			    src_location:Point:srid=4326,			    order_terminate_pid:int,"
				+ "			    kuaidi_oid:int			";

		public final static String test_sch= "orderId:String:index=full,"+
																			        "driverId:String,"+
																			        "userId:String,"+
																			        "ts:java.lang.Long,"+
																			        "update:java.lang.Long,"+       
																			        "*geom:Point:srid=4326";
}

[system]

sql=with (\
select 1 intfId, from_unixtime(AGGRTIME DIV 1000,'yyyyMMddHHmmss') as logtime, count(if(Value_ > 0, 1, NULL)) totalcount from log_10059 group by 1 coordinate by TimeStamp_*1000 with aggr interval 10 seconds union all \
select 2 intfId, from_unixtime(AGGRTIME DIV 1000,'yyyyMMddHHmmss') as logtime, count(if(Value_ < 0, 1, NULL)) totalcount from log_10059 group by 1 coordinate by TimeStamp_*1000 with aggr interval 10 seconds union all \
select 3 intfId, from_unixtime(AGGRTIME DIV 1000,'yyyyMMddHHmmss') as logtime, count(1) totalcount from log_10026 group by 1 coordinate by TimeStamp_*1000 with aggr interval 10 seconds union all \
select 4 intfId, from_unixtime(AGGRTIME DIV 1000,'yyyyMMddHHmmss') as logtime, count(1) totalcount from log_10377 group by 1 coordinate by TimeStamp_*1000 with aggr interval 10 seconds union all \
select 5 intfId, from_unixtime(AGGRTIME DIV 1000,'yyyyMMddHHmmss') as logtime, count(if(ActionType_ = 0, 1, null)) totalcount from log_10380 group by 1 coordinate by TimeStamp_*1000 with aggr interval 10 seconds union all \
select 6 intfId, from_unixtime(AGGRTIME DIV 1000,'yyyyMMddHHmmss') as logtime, count(if(ActionType_ <> 0, 1, null)) totalcount from log_10380 group by 1 coordinate by TimeStamp_*1000 with aggr interval 10 seconds union all \
select 7 intfId, from_unixtime(AGGRTIME DIV 1000,'yyyyMMddHHmmss') as logtime, count(1) totalcount from log_10013 group by 1 coordinate by TimeStamp_*1000 with aggr interval 10 seconds \
) monitorres \
insert into moniter_dest select concat("bid=BOSS_MONITOR&buID=&sysID=9900001&intfID=", intfId, "&logtime=", logtime, "&srcIp=&dstIp=&retType=0&errCode=0&latency=0&totalcount=", totalcount) from monitorres, \
insert into moniter_dest select concat("bid=BOSS_MONITOR&buID=&sysID=9900001&intfID=8&logtime=", from_unixtime(AGGRTIME DIV 1000,'yyyyMMddHHmmss'), "&CallBackApiType=", UseCallBackApi_, "&srcIp=&dstIp=&retType=0&errCode=0&latency=0&totalcount=", count(1)) from log_10348 group by UseCallBackApi_ coordinate by TimeStamp_*1000 with aggr interval 10 seconds 






[tabledesc-log-10013]
table.name=log_10013
table.binary.mode=false
table.fields=LogID_,BIGINT,:ProtocolVersion_,BIGINT,:Uin_,BIGINT,:IfSuccess_,BIGINT,:Device_,BIGINT,:ClientVersion_,BIGINT,:ClientIP_,BIGINT,:AgentIP_,BIGINT,:TimeStamp_,BIGINT,:ExpandCol1_,BIGINT,:ExpandCol2_,BIGINT,:MsgType_,BIGINT,:FromUin_,BIGINT,
table.field.splitter=,
table.list.splitter=;
table.map.splitter=:
table.zk.servers=tl-zk-wx1:2181,tl-zk-wx2:2181,tl-zk-wx3:2181,tl-zk-wx4:2181,tl-zk-wx5:2181
table.zk.root=/meta_weixin
table.topic=wxg_weixin_msg_oauth
table.interfaceId=10013

[tabledesc-log-10026]
table.name=log_10026
table.binary.mode=false
table.fields=LogID_,BIGINT,:ProtocolVersion_,BIGINT,:Uin_,BIGINT,:IfSuccess_,BIGINT,:Device_,BIGINT,:ClientVersion_,BIGINT,:ClientIP_,BIGINT,:AgentIP_,BIGINT,:TimeStamp_,BIGINT,:ExpandCol1_,BIGINT,:ExpandCol2_,BIGINT,:BizMsgType_,BIGINT,:CheckType_,BIGINT,:MsgID_,BIGINT,
table.field.splitter=,
table.list.splitter=;
table.map.splitter=:
table.zk.servers=tl-zk-wx1:2181,tl-zk-wx2:2181,tl-zk-wx3:2181,tl-zk-wx4:2181,tl-zk-wx5:2181
table.zk.root=/meta_weixin
table.topic=wxg_weixin_msg_oauth
table.interfaceId=10026

[tabledesc-log-10059]
table.name=log_10059
table.binary.mode=false
table.fields=LogID_,BIGINT,:ProtocolVersion_,BIGINT,:Uin_,BIGINT,:IfSuccess_,BIGINT,:Device_,BIGINT,:ClientVersion_,BIGINT,:ClientIP_,BIGINT,:AgentIP_,BIGINT,:TimeStamp_,BIGINT,:ExpandCol1_,BIGINT,:ExpandCol2_,BIGINT,:Value_,BIGINT,:FriendUin_,BIGINT,:Scene_,BIGINT,:QrScene_,BIGINT,
table.field.splitter=,
table.list.splitter=;
table.map.splitter=:
table.zk.servers=tl-zk-wx1:2181,tl-zk-wx2:2181,tl-zk-wx3:2181,tl-zk-wx4:2181,tl-zk-wx5:2181
table.zk.root=/meta_weixin
table.topic=wxg_weixin_msg_oauth
table.interfaceId=10059

[tabledesc-log-10348]
table.name=log_10348
table.binary.mode=false
table.fields=LogID_,BIGINT,:ProtocolVersion_,BIGINT,:Uin_,BIGINT,:IfSuccess_,BIGINT,:Device_,BIGINT,:ClientVersion_,BIGINT,:ClientIP_,BIGINT,:AgentIP_,BIGINT,:TimeStamp_,BIGINT,:ExpandCol1_,BIGINT,:ExpandCol2_,BIGINT,:UseCallBackApi_,BIGINT,:CallBackUrl_,STRING,:CallBackHost_,STRING,:CalkBackTimeCost_,BIGINT,:VerifyToken_,BIGINT,
table.field.splitter=,
table.list.splitter=;
table.map.splitter=:
table.zk.servers=tl-zk-wx1:2181,tl-zk-wx2:2181,tl-zk-wx3:2181,tl-zk-wx4:2181,tl-zk-wx5:2181
table.zk.root=/meta_weixin
table.topic=wxg_weixin_msg_oauth
table.interfaceId=10348

[tabledesc-log-10377]
table.name=log_10377
table.binary.mode=false
table.fields=LogID_,BIGINT,:ProtocolVersion_,BIGINT,:Uin_,BIGINT,:IfSuccess_,BIGINT,:Device_,BIGINT,:ClientVersion_,BIGINT,:ClientIP_,BIGINT,:AgentIP_,BIGINT,:TimeStamp_,BIGINT,:ExpandCol1_,BIGINT,:ExpandCol2_,BIGINT,:bizuin_,BIGINT,:msgid_,BIGINT,:itemidx_,BIGINT,:scene_,BIGINT,:title_,STRING,:platform_,BIGINT,
table.field.splitter=,
table.list.splitter=;
table.map.splitter=:
table.zk.servers=tl-zk-wx1:2181,tl-zk-wx2:2181,tl-zk-wx3:2181,tl-zk-wx4:2181,tl-zk-wx5:2181
table.zk.root=/meta_weixin
table.topic=wxg_weixin_msg_oauth
table.interfaceId=10377

[tabledesc-log-10380]
table.name=log_10380
table.binary.mode=false
table.fields=LogID_,BIGINT,:ProtocolVersion_,BIGINT,:Uin_,BIGINT,:IfSuccess_,BIGINT,:Device_,BIGINT,:ClientVersion_,BIGINT,:ClientIP_,BIGINT,:AgentIP_,BIGINT,:TimeStamp_,BIGINT,:ExpandCol1_,BIGINT,:ExpandCol2_,BIGINT,:bizuin_,BIGINT,:url_,STRING,:targeturl_,BIGINT,:MsgID_,BIGINT,:ItemIdx_,BIGINT,:ActionType_,BIGINT,
table.field.splitter=,
table.list.splitter=;
table.map.splitter=:
table.zk.servers=tl-zk-wx1:2181,tl-zk-wx2:2181,tl-zk-wx3:2181,tl-zk-wx4:2181,tl-zk-wx5:2181
table.zk.root=/meta_weixin
table.topic=wxg_weixin_msg_oauth
table.interfaceId=10380




[tabledesc-log-moniter_dest]
table.name=moniter_dest_bak
table.fields=value,string,
table.field.splitter=,
table.list.splitter=;
table.map.splitter=:
table.zk.servers=sk-zk-td1:2181,sk-zk-td2:2181,sk-zk-td3:2181,sk-zk-td4:2181,sk-zk-td5:2181
table.zk.root=/meta_mon
table.topic=gdtjk
table.interfaceId=wx_jk


[tabledesc-log-moniter_dest_tube]
table.name=moniter_dest
table.type=tube
table.fields=value,string,
table.field.splitter=,
table.list.splitter=;
table.map.splitter=:
table.tube.master=sk-share-tube-master
table.tube.port=8609
table.topic=gdtjk
table.interfaceId=wx_jk


CREATE DATABASE IF NOT EXISTS yuchao;

use yuchao;


create table t_system_config (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    `name` varchar(128) not null,
    `config_key` varchar(128) not null,
    `config_value` varchar(1024) not null,
    unique key uk_key (`key`)
) comment '系统配置表';


CREATE TABLE `distributed_lock` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    `resource_key` varchar(64) NOT NULL COMMENT '资源标识（如订单ID、座位ID）',
    `holder_id` varchar(64) NOT NULL COMMENT '锁持有者标识（如进程ID、UUID）',
    `expire_time` datetime NOT NULL COMMENT '锁过期时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_resource_key` (`resource_key`) COMMENT '唯一索引：保证同一资源只能有一条锁记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分布式锁表';

create table t_account (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    username varchar(128) not null,
    password varchar(128),
    name varchar(32),
    role varchar(32),
    tenant_id varchar(32),
    remark text,
    unique key uk_username (username)
) comment '系统管理员';

-- password: 123456
insert into t_account (username, password, name) values('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员');
insert into t_account (username, password, name) values('lf', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员');

create table t_match (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    name varchar(128),
    detail text,
    cover varchar(1024),
    matinee_name varchar(128) comment '场次名字',
    status varchar(32) not null default 'DISABLE',
    sale_status varchar(32) not null default 'NOT_FINISH',
    venue_id bigint not null default 0,
    buy_limit int not null default 2,
    gift_ticket_url varchar(1024) comment '赠票oss地址',
    gate_url varchar(1024) comment '闸机调用地址',
    gate_token varchar(1024) comment '闸机调用Token',
    start_sale_time datetime comment '开始售票时间',
    start_time datetime,
    end_time datetime
) comment '赛程表';


create table t_venue (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    name varchar(128),
    detail varchar(1024),
    venue_address varchar(512) comment '场馆地址',
    venue_lng varchar(32) comment '场馆经度',
    venue_lat varchar(32) comment '场馆纬度',
    province_name varchar(32) comment '省份',
    province_code varchar(32) comment '省份',
    city_name varchar(32) comment '市',
    city_code varchar(32) comment '市',
    area_name varchar(32) comment '区',
    area_code varchar(32) comment '区',
    sale_sku_info text comment 'sku源信息',
    gift_sku_info text comment 'sku源信息'
) comment '场馆表';

INSERT INTO `t_venue` (`id`, `name`, `detail`, `venue_address`, `venue_lng`, `venue_lat`, `province_name`, `province_code`, `city_name`, `city_code`, `area_name`, `area_code`, `sale_sku_info`, `gift_sku_info`)
VALUES ('1', '彭水自治县新城体育中心体育场', 'xxx', '重庆市彭水苗族土家族自治县插旗街与滨江路交叉口西北180米', '108.158791', '29.286262', '重庆', '50', '重庆', '5002', '渝中', '500243',
'[{"skuName":"A区","area":"A区","price":0,"totalTicket":542},
{"skuName":"B区","area":"B区","price":0,"totalTicket":541},
{"skuName":"C区","area":"C区","price":0,"totalTicket":624},
{"skuName":"E区","area":"E区","price":0,"totalTicket":1239},
{"skuName":"F区","area":"F区","price":0,"totalTicket":1416},
{"skuName":"G区","area":"G区","price":0,"totalTicket":1995}]',

'[{"skuName":"A区","area":"A区","price":0,"totalTicket":567},
{"skuName":"B区","area":"B区","price":0,"totalTicket":567},
{"skuName":"C区","area":"C区","price":0,"totalTicket":386},
{"skuName":"D区","area":"D区","price":0,"totalTicket":2538},
{"skuName":"E区","area":"E区","price":0,"totalTicket":381},
{"skuName":"F区","area":"F区","price":0,"totalTicket":156},
{"skuName":"G区","area":"G区","price":0,"totalTicket":570}]');


create table t_sku (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    match_id bigint not null default 0,
    venue_id bigint not null default 0,
    sku_name varchar(32),
    sku_type varchar(32) not null,
    area varchar(32),
    price decimal(10, 2) default 0,
    total_ticket int not null default 0,
    stock_ticket int not null default 0,
    INDEX idx_match_id (`match_id`)
) comment 'sku';


create table t_order (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    order_no varchar(32),
    match_id bigint not null default 0,
    venue_id bigint not null default 0,
    user_id bigint not null default 0,
    sku_id bigint not null default 0,
    buy_num int,
    total_price decimal(10, 2) default 0,
    unique key uk_order_no (`order_no`),
    INDEX idx_user_id_gmt_create (`user_id`, `gmt_create`),
    INDEX idx_match_id_gmt_create (`match_id`, `gmt_create`)
) comment '订单表';


create table t_ticket (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    bid varchar(32) comment '用于生成二维码的UID',
    match_id bigint not null default 0,
    venue_id bigint not null default 0,
    sku_id bigint not null default 0,
    order_id bigint default null,
    buyer_id bigint default null,
    name varchar(32),
    id_type varchar(32),
    id_no varchar(32),
    mobile varchar(32),
    area varchar(32) not null comment '区域',
    sub_area varchar(32) not null comment '子区域',
    seat_row int not null default 0 comment '行',
    seat_no int not null default 0 comment '号',
    ticket_type varchar(32) not null,
    sale_status varchar(32) not null default 'UNSOLD',
    sync_status varchar(32) not null default 'NOT_SYNC',
    price decimal(10, 2) default 0 ,
    sale_time datetime comment '售卖时间',
    unique key uk_bid (`bid`),
    unique key uk_id_no_match_id (`id_no`, `match_id`),
    unique key uk_sku_id_sub_area_seat (`sku_id`, `sub_area`, `seat_row`, `seat_no`),
    INDEX idx_order_id (`order_id`),
    INDEX idx_sku_id (`sku_id`),
    INDEX idx_sale_time (`sale_time`)
) comment '票';


create table t_user (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    openid varchar(128),
    name varchar(32),
    id_type varchar(32),
    id_no varchar(32),
    mobile varchar(32),
    nickname varchar(64),
    avatar_url varchar(128),
    unique key uk_openid (`openid`),
    unique key uk_id_no (`id_no`)
) comment '用户表';



INSERT INTO `yuchao`.`t_match`(`gate_url`, `gate_token`, `name`, `detail`, `cover`, `matinee_name`, `status`, `sale_status`, `venue_id`, `gift_ticket_url`, `start_time`, `end_time`, `start_sale_time`)
VALUES ('http://yc1.zszlchina.com/es-server/api/push/person', 'b095bb12d6b844c995be85473cd45cc5',
'“新韵重庆 渝超同行”2025 重庆城市足球超级联赛一渝东南赛区彭水县主赛场',
'<div style=\"text-align:left\"><img src=\"https://cy25yuchao.oss-cn-chengdu.aliyuncs.com/images/181264b0-500d-4635-b6df-adc18c4a3d53.jpg\" alt=\"图片 alt\" width=\"100%\" height=\"auto\" data-align=\"left\"></div><p></p>',
'https://cy25yuchao.oss-cn-chengdu.aliyuncs.com/images/527577f6-ca7a-4d34-9d7f-073dde073d70.jpg',
NULL, 'ENABLE', 'NOT_FINISH', 1, 'https://cy25yuchao.oss-cn-chengdu.aliyuncs.com/upload/20250925_213205-ticket.zip', '2025-10-01 10:00:00', '2025-10-08 10:00:00', '2025-10-08 18:00:00');



INSERT INTO `t_user` (`openid`, `name`, `id_type`, `id_no`, `mobile`, `nickname`, `avatar_url`)
VALUES ('1', '张三丰', 'ID_CARD', '1510823199922223333', '18712345678', NULL, NULL);



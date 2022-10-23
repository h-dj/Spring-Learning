-- distribute_id.crm_number_config definition

CREATE TABLE `crm_number_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置id',
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `code` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置业务编码',
  `sort` int NOT NULL COMMENT '编号顺序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='系统自动生成编号配置表';


-- distribute_id.crm_number_setting definition

CREATE TABLE `crm_number_setting` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '设置id',
  `code` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置业务编码',
  `sort` int NOT NULL COMMENT '编号顺序',
  `type` int NOT NULL COMMENT '编号类型 1文本 2日期 3数字 4json映射',
  `value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文本内容或日期格式或起始编号或json数据格式',
  `increase_number` int DEFAULT NULL COMMENT '递增数',
  `reset_type` int DEFAULT NULL COMMENT '重新编号周期 1每天 2每月 3每年 4从不',
  `last_number` int DEFAULT NULL COMMENT '上次生成的编号',
  `last_date` date DEFAULT NULL COMMENT '上次生成的时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='系统自动生成编号设置表';
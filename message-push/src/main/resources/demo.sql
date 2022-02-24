-- message_push.sys_message_content definition

CREATE TABLE `sys_message_content` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `context` text COMMENT '消息内容',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `send_user_id` int DEFAULT NULL COMMENT '发送者ID',
  `title` varchar(512) DEFAULT NULL COMMENT '消息标题',
  `message_type` char(1) DEFAULT NULL COMMENT '消息类型: 1公告，2通知',
  `is_delete` char(1) DEFAULT '0' COMMENT '是否删除: 0否，1是',
  `module_id` int DEFAULT NULL COMMENT '消息来源模块',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息内容表';


-- message_push.sys_message_template definition

CREATE TABLE `sys_message_template` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type` char(1) DEFAULT NULL COMMENT '模板类型: 1sms、2email、3 微信公众号消息、4系统站内信',
  `content` text COMMENT '模板内容',
  `is_delete` char(1) DEFAULT '0' COMMENT '是否删除: 0否、1是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `update_user_id` int DEFAULT NULL COMMENT '更新用户ID',
  `title` varchar(100) DEFAULT NULL COMMENT '模板标题',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统消息模板';


-- message_push.sys_message_type definition

CREATE TABLE `sys_message_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '消息类型ID',
  `category` char(1) DEFAULT NULL COMMENT '消息分类：1公告、2、通知',
  `name` varchar(100) DEFAULT NULL COMMENT '消息类型名称',
  `message_type_code` varchar(100) DEFAULT NULL COMMENT '消息类型编码',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_delete` char(1) DEFAULT NULL COMMENT '是否删除: 0否、1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息类型';


-- message_push.sys_user_message_notice definition

CREATE TABLE `sys_user_message_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `message_content_id` int DEFAULT NULL COMMENT '消息内容表ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_read` char(1) DEFAULT '0' COMMENT '是否已读：0否，1是',
  `is_delete` char(1) DEFAULT '0' COMMENT '是否删除：0否，1是',
  `receiver_user_id` int DEFAULT NULL COMMENT '接收消息用户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户消息通知';


-- message_push.sys_user_message_setting definition

CREATE TABLE `sys_user_message_setting` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '消息设置ID',
  `email` char(1) DEFAULT NULL COMMENT '邮件发送：0关闭、1开启',
  `sms` char(1) DEFAULT NULL COMMENT '短信发送：0关闭、1开启',
  `web` char(1) DEFAULT NULL COMMENT '系统站内信：0关闭、1开启',
  `wechat` char(1) DEFAULT NULL COMMENT '微信公众号消息：0关闭、1开启',
  `message_type_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '消息类型编码',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户消息提醒设置';
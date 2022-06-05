-- 水平分表
DROP TABLE IF EXISTS `course_1`;
CREATE TABLE `course_1` (
  `ID` bigint NOT NULL,
  `CNO` varchar(5) NOT NULL COMMENT '课程号',
  `CNAME` varchar(104) DEFAULT NULL COMMENT '课程名称',
  `TNO` varchar(10) DEFAULT NULL COMMENT '老师编号',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `COURSE_un` (`CNO`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表1';

DROP TABLE IF EXISTS `course_2`;
CREATE TABLE `course_2` (
  `ID` bigint NOT NULL,
  `CNO` varchar(5) NOT NULL COMMENT '课程号',
  `CNAME` varchar(104) DEFAULT NULL COMMENT '课程名称',
  `TNO` varchar(10) DEFAULT NULL COMMENT '老师编号',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `COURSE_un` (`CNO`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表2';


-- 水平分库分表

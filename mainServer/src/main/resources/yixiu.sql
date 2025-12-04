/*
 Navicat Premium Data Transfer

 Source Server         : 阿里云mysql8
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : 8.148.253.180:3306
 Source Schema         : yixiu

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 30/11/2025 17:07:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base`  (
  `knowledge_id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识编号',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识标题',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类（系统、硬件、软件等）',
  `problem_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题描述',
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解决方案',
  `source` enum('manual_entry','auto_extracted','user_log') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'user_log' COMMENT '数据来源',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`knowledge_id`) USING BTREE,
  INDEX `idx_knowledge_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI义修助手知识库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_query_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_query_log`;
CREATE TABLE `ai_query_log`  (
  `query_id` bigint NOT NULL AUTO_INCREMENT COMMENT '查询编号',
  `user_id` bigint NOT NULL COMMENT '提问用户',
  `query_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提问内容',
  `ai_response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI回复内容',
  `related_knowledge_id` bigint NULL DEFAULT NULL COMMENT '关联知识ID',
  `query_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '查询时间',
  PRIMARY KEY (`query_id`) USING BTREE,
  INDEX `fk_query_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_query_knowledge`(`related_knowledge_id` ASC) USING BTREE,
  CONSTRAINT `fk_query_knowledge` FOREIGN KEY (`related_knowledge_id`) REFERENCES `ai_knowledge_base` (`knowledge_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_query_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI问答记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈编号',
  `request_id` bigint NOT NULL COMMENT '报修单编号',
  `user_id` bigint NOT NULL COMMENT '评论人',
  `rating` int NULL DEFAULT 5 COMMENT '评分（1~5）',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文字评论',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`feedback_id`) USING BTREE,
  INDEX `fk_feedback_request`(`request_id` ASC) USING BTREE,
  INDEX `fk_feedback_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_feedback_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for repair_assignment
-- ----------------------------
DROP TABLE IF EXISTS `repair_assignment`;
CREATE TABLE `repair_assignment`  (
  `assign_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分配编号',
  `request_id` bigint NOT NULL COMMENT '报修单号',
  `volunteer_id` bigint NOT NULL COMMENT '维修志愿者编号',
  `is_leader` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为队长（1=是，0=否）',
  `assigned_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态：0待进行，1维修中，2维修完成，3未解决已完成，4任务取消',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注说明',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`assign_id`) USING BTREE,
  INDEX `fk_assign_request`(`request_id` ASC) USING BTREE,
  INDEX `fk_assign_volunteer`(`volunteer_id` ASC) USING BTREE,
  INDEX `idx_assign_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_assign_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_assign_volunteer` FOREIGN KEY (`volunteer_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修任务分配表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for repair_log
-- ----------------------------
DROP TABLE IF EXISTS `repair_log`;
CREATE TABLE `repair_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志编号',
  `volunteer_id` bigint NOT NULL COMMENT '维修队员',
  `request_id` bigint NOT NULL COMMENT '报修单编号',
  `log_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维修详细内容',
  `repair_duration` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维修耗时（例如：2小时30分钟）',
  `solution_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解决方案摘要',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `fk_log_volunteer`(`volunteer_id` ASC) USING BTREE,
  INDEX `fk_log_request`(`request_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_log_volunteer` FOREIGN KEY (`volunteer_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for repair_log_img
-- ----------------------------
DROP TABLE IF EXISTS `repair_log_img`;
CREATE TABLE `repair_log_img`  (
  `img_id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片编号',
  `log_id` bigint NOT NULL COMMENT '关联的维修日志编号',
  `img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维修过程图片URL',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`img_id`) USING BTREE,
  INDEX `idx_log_img_log_id`(`log_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_img_log` FOREIGN KEY (`log_id`) REFERENCES `repair_log` (`log_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修日志图片表（存放维修过程照片）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for repair_request
-- ----------------------------
DROP TABLE IF EXISTS `repair_request`;
CREATE TABLE `repair_request`  (
  `request_id` bigint NOT NULL AUTO_INCREMENT COMMENT '报修编号',
  `user_id` bigint NOT NULL COMMENT '报修人',
  `contact_type` enum('wechat','phone','email') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式类型（wechat: 微信, phone: 手机号, email: 邮箱）',
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系账号或号码',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备类型',
  `device_system` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备系统',
  `device_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备型号',
  `problem_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '故障描述',
  `campus` tinyint(1) NOT NULL DEFAULT 0 COMMENT '校区（0: 南校区，1: 北校区）',
  `repair_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维修地点',
  `appointment_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预约维修时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '报修状态（0:已提交待审核，1:审核通过，2:已被接收，3:已完成，4:已取消，5:用户自行解决，6:已被拒绝）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`request_id`) USING BTREE,
  INDEX `fk_request_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_request_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_request_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修请求表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for repair_request_img
-- ----------------------------
DROP TABLE IF EXISTS `repair_request_img`;
CREATE TABLE `repair_request_img`  (
  `img_id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片编号',
  `request_id` bigint NOT NULL COMMENT '关联的报修单编号',
  `img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL地址',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`img_id`) USING BTREE,
  INDEX `idx_request_img_request_id`(`request_id` ASC) USING BTREE,
  CONSTRAINT `fk_request_img_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修请求图片表（每个报修单可对应多张故障图片）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户编号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名（非微信注册可用）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码（非微信注册可用）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信OpenID（小程序内唯一）',
  `unionid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信UnionID（跨平台唯一，可选）',
  `wx_nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信昵称',
  `wx_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信头像URL',
  `role` enum('student','volunteer','admin','super_admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'student' COMMENT '角色类型',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态：0离线，1正常，2退队，3退休，4冻结',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login` datetime NULL DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表（支持微信登录）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for volunteer_info
-- ----------------------------
DROP TABLE IF EXISTS `volunteer_info`;
CREATE TABLE `volunteer_info`  (
  `volunteer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者ID',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID',
  `student_number` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号（11位数字，唯一）',
  `major_class` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '班级',
  `grade` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级（四位年份）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`volunteer_id`) USING BTREE,
  UNIQUE INDEX `uk_student_number`(`student_number` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_volunteer_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿者信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Triggers structure for table repair_assignment
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_repair_assignment_update_time`;
delimiter ;;
CREATE TRIGGER `trg_repair_assignment_update_time` BEFORE UPDATE ON `repair_assignment` FOR EACH ROW BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table repair_request
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_repair_request_update_time`;
delimiter ;;
CREATE TRIGGER `trg_repair_request_update_time` BEFORE UPDATE ON `repair_request` FOR EACH ROW BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table users
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_users_update_time`;
delimiter ;;
CREATE TRIGGER `trg_users_update_time` BEFORE UPDATE ON `users` FOR EACH ROW BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

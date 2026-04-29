package com.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudResourceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudResourceManagementApplication.class, args);
        System.out.println("========================================");
        System.out.println("云资源管理系统启动成功!");
        System.out.println("========================================");
        System.out.println("后端服务端口: 8002");
        System.out.println("API 文档: http://localhost:8002/");
        System.out.println("========================================");
        System.out.println("默认测试客户:");
        System.out.println("  客户ID: C001, 名称: 张三, 余额: 5000.00");
        System.out.println("  客户ID: C002, 名称: 李四, 余额: 1000.00");
        System.out.println("========================================");
    }
}

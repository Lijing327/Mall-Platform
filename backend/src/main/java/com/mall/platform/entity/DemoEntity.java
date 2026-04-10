package com.mall.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 演示实体：仅用于说明 MyBatis-Plus 分层结构，不承载具体业务。
 */
@TableName("demo_table")
public class DemoEntity extends BaseEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

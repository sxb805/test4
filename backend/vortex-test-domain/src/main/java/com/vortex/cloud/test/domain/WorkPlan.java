package com.vortex.cloud.test.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.vortex.cloud.test.support.Constants;
import com.vortex.cloud.vfs.lite.data.domain.AbstractBaseDeleteModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

/**
 * 工作计划表
 */
@Data
@EqualsAndHashCode(of = "id")
@Entity(name = WorkPlan.TABLE_NAME)
@Table(name = WorkPlan.TABLE_NAME)
@Comment(value = "工作计划表")
@TableName(value = WorkPlan.TABLE_NAME)
public class WorkPlan extends AbstractBaseDeleteModel {

    public static final String TABLE_NAME = Constants.TABLE_PREFIX + "work_plan";

    @Column(columnDefinition = "varchar(64) comment '项目ID'")
    private String projectId;

    @Column(columnDefinition = "varchar(64) comment '项目编号'")
    private String projectNo;

    @Column(columnDefinition = "varchar(200) comment '项目名称'")
    private String projectName;

    @Column(columnDefinition = "int comment '年份'")
    private Integer year;

    @Column(columnDefinition = "decimal(10,3) comment '一季度（人/次）'")
    private BigDecimal firstQuarterPersonTimes;

    @Column(columnDefinition = "decimal(10,3) comment '二季度（人/次）'")
    private BigDecimal secondQuarterPersonTimes;

    @Column(columnDefinition = "decimal(10,3) comment '三季度（人/次）'")
    private BigDecimal thirdQuarterPersonTimes;

    @Column(columnDefinition = "decimal(10,3) comment '四季度（人/次）'")
    private BigDecimal fourthQuarterPersonTimes;
}

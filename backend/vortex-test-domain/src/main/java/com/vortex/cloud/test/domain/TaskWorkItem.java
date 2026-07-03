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

import java.time.LocalDate;

/**
 * 任务工单表
 */
@Data
@EqualsAndHashCode(of = "id")
@Entity(name = TaskWorkItem.TABLE_NAME)
@Table(name = TaskWorkItem.TABLE_NAME)
@Comment(value = "任务工单表")
@TableName(value = TaskWorkItem.TABLE_NAME)
public class TaskWorkItem extends AbstractBaseDeleteModel {

    public static final String TABLE_NAME = Constants.TABLE_PREFIX + "task_work_item";

    @Column(columnDefinition = "varchar(64) comment '项目ID'")
    private String projectId;

    @Column(columnDefinition = "varchar(64) comment '项目编号'")
    private String projectNo;

    @Column(columnDefinition = "varchar(200) comment '项目名称'")
    private String projectName;

    @Column(columnDefinition = "varchar(20) comment '项目类型'")
    private String projectType;

    @Column(columnDefinition = "varchar(64) comment '所属TL ID'")
    private String ownerTlId;

    @Column(columnDefinition = "varchar(50) comment '所属TL名称'")
    private String ownerTlName;

    @Column(columnDefinition = "varchar(100) comment '模块'")
    private String moduleName;

    @Column(columnDefinition = "varchar(2000) comment '任务描述'")
    private String taskDesc;

    @Column(columnDefinition = "date comment '开始日期'")
    private LocalDate startDate;

    @Column(columnDefinition = "date comment '结束日期'")
    private LocalDate endDate;

    @Column(columnDefinition = "int comment '预计工时'")
    private Integer estimatedHours;

    @Column(columnDefinition = "varchar(64) comment '责任人ID'")
    private String ownerId;

    @Column(columnDefinition = "varchar(50) comment '责任人'")
    private String ownerName;

    @Column(columnDefinition = "varchar(20) comment '完成状态'")
    private String status;

    @Column(columnDefinition = "date comment '实际完成日期'")
    private LocalDate actualFinishDate;

    @Column(columnDefinition = "int comment '实际工时'")
    private Integer actualHours;

    @Column(columnDefinition = "varchar(64) comment '实际完成人ID'")
    private String actualOwnerId;

    @Column(columnDefinition = "varchar(50) comment '实际完成人'")
    private String actualOwnerName;

    @Column(columnDefinition = "varchar(2000) comment '任务进度跟进描述'")
    private String progressNote;
}

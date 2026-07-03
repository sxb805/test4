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

/**
 * 项目表
 */
@Data
@EqualsAndHashCode(of = "id")
@Entity(name = Project.TABLE_NAME)
@Table(name = Project.TABLE_NAME)
@Comment(value = "项目表")
@TableName(value = Project.TABLE_NAME)
public class Project extends AbstractBaseDeleteModel {

    public static final String TABLE_NAME = Constants.TABLE_PREFIX + "project";

    @Column(columnDefinition = "varchar(32) comment '编号'")
    private String code;

    @Column(columnDefinition = "varchar(100) comment '名称'")
    private String name;

    @Column(columnDefinition = "varchar(20) comment '类型'")
    private String type;

    @Column(columnDefinition = "varchar(64) comment 'TL人员ID'")
    private String tlId;

    @Column(columnDefinition = "varchar(50) comment 'TL名称'")
    private String tlName;
}

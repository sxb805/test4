package com.vortex.cloud.test.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vortex.cloud.test.support.Constants;
import com.vortex.cloud.vfs.lite.data.domain.AbstractBaseDeleteModel;
import com.vortex.cloud.vfs.lite.data.handler.GeometryTypeHandler;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author zhanglei
 */
@Data
@EqualsAndHashCode(of = "id")
@Entity(name = Example.TABLE_NAME)
@Table(name = Example.TABLE_NAME, indexes = {
        @Index(name = "idx_buildDate", columnList = "buildDate"),
})
@Comment(value = "样例表")
@TableName(value = Example.TABLE_NAME)
public class Example extends AbstractBaseDeleteModel {

    public static final String TABLE_NAME = Constants.TABLE_PREFIX + "example";

    @Column(columnDefinition = "varchar(50) comment '编码'")
    private String code;

    @Column(columnDefinition = "varchar(100) comment '名称'")
    private String name;

    @Column(columnDefinition = "varchar(50) comment '类型，租户参数编码：param_example_type'")
    private String type;

    @Column(columnDefinition = "date comment '建设日期'")
    private LocalDate buildDate;

    @Column(columnDefinition = "datetime comment '建设时间'")
    private LocalDateTime buildTime;

    @Column(columnDefinition = "varchar(50) comment '日期类型，参考：ExampleEnum'")
    private String dateType;

    @Column(columnDefinition = "bit comment '是否离线'")
    private Boolean hasOffline;

    @Column(columnDefinition = "varchar(50) comment '管理人员ID'")
    private String managerStaffId;

    @Column(columnDefinition = "decimal(10,2) comment '金额'")
    private BigDecimal amount;

    @Column(columnDefinition = "double(10,4) comment '版本'")
    private Double version;

    @Column(columnDefinition = "text comment '附件，json数组'")
    private String files;

    @Column(columnDefinition = "varchar(500) comment '地址'")
    private String address;

    @TableField(value = "location", typeHandler = GeometryTypeHandler.class)
    @Column(columnDefinition = "geometry comment '地理信息'")
    private Geometry location;

}

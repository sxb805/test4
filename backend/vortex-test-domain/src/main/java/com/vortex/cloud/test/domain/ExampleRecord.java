package com.vortex.cloud.test.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.collect.Lists;
import com.vortex.cloud.test.support.Constants;
import com.vortex.cloud.vfs.lite.data.domain.AbstractBaseDeleteModel;
import com.vortex.cloud.vfs.lite.data.dto.IndexDefinitionDTO;
import com.vortex.cloud.vfs.lite.data.handler.GeometryTypeHandler;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 *   样例记录
 *   @author donghao
 *   @date 2025/4/22 10:45
 */
@Data
@EqualsAndHashCode(of = "id")
@Entity(name = ExampleRecord.TABLE_NAME)
@Table(name = ExampleRecord.TABLE_NAME)
@Comment(value = "管网健康度评分月度数据")
@TableName(value = ExampleRecord.TABLE_NAME)
public class ExampleRecord  extends AbstractBaseDeleteModel {
    public static final String TABLE_NAME = Constants.TABLE_PREFIX + "example_record";

    @Column(columnDefinition = "date comment 'yyyy-MM'")
    private Date month;

    @Column(columnDefinition = "varchar(50) comment '编码'")
    private String code;

    @Column(columnDefinition = "varchar(100) comment '名称'")
    private String name;

    @Column(columnDefinition = "varchar(50) comment '类型，租户参数编码：param_example_type'")
    private String type;

    @Column(columnDefinition = "date comment '建设日期'")
    private LocalDate buildDate;

    @Column(columnDefinition = "varchar(50) comment '日期类型，参考：ExampleEnum'")
    private String dateType;

    @Column(columnDefinition = "tinyint comment '状态 1.正常 2.禁用'")
    private Integer status;

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

    @TableField(value = "location", typeHandler = GeometryTypeHandler.class)
    @Column(columnDefinition = "geometry comment '地理信息'")
    private Geometry location;

    public static List<IndexDefinitionDTO> indexDefinitions() {
        return Lists.newArrayList(
                IndexDefinitionDTO.builder()
                        .indexName("idx_code")
                        .columnNames(Lists.newArrayList("code"))
                        .build(),
                IndexDefinitionDTO.builder()
                        .indexName("idx_name")
                        .columnNames(Lists.newArrayList("name"))
                        .build()
        );
    }
}

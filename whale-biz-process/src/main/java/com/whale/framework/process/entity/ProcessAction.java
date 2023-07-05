package com.whale.framework.process.entity;

import com.magus.framework.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.entity
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/6 11:44 AM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_action")
@ApiModel("流程操作权限")
public class ProcessAction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "code")
    @ApiModelProperty(value = "编码")
    private String code;

    @Column(name = "name")
    @ApiModelProperty(value = "名称")
    private String name;

    @Column(name = "sys_flag")
    @ApiModelProperty(value = "是否是系统默认(true:系统；false:自定义)")
    private Boolean sysFlag;
}

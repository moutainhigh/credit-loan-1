package com.sixliu.creditloan.workflow.entity;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author:MG01867
 * @date:2018年6月15日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 基础po
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AuditBaseEntity extends BaseEntity{

	/**数据版本:int(11)**/
	private int version;
	
	/**数据更新用户id:VARCHAR(36)**/
	private String updateUserId;
	
	/**数据更新日期(每次写操作时赋值):TIMESTAMP**/
	private Date updateDate;
	
	/**数据创建用户id:VARCHAR(36)**/
	private String createUserId;
	
	/**数据创建日期(只在创建时赋值):TIMESTAMP**/
	private Date createDate;
}

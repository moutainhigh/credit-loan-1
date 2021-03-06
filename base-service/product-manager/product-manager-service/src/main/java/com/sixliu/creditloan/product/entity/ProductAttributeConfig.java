package com.sixliu.creditloan.product.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author:MG01867
 * @date:2018年6月15日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 产品扩展属性
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductAttributeConfig extends BaseEntity{
	
	/** 所属者id:VARCHAR(36)**/
	private String productId;
	
	/** 扩展属性名称:VARCHAR(20)**/
	private String group;
	
	/** 扩展属性index:int(11)**/
	private Integer index;
	
	/** 扩展属性名称:VARCHAR(20)**/
	private String name;

	/** 扩展属性值:VARCHAR(200)**/
	private String value;
}

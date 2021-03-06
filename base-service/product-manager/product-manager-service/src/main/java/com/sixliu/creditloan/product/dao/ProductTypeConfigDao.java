package com.sixliu.creditloan.product.dao;

import java.util.List;

import com.sixliu.creditloan.product.entity.ProductTypeConfig;

/**
*@author:MG01867
*@date:2018年6月15日
*@E-mail:359852326@qq.com
*@version:
*@describe 产品类型配置数据访问接口
*/
public interface ProductTypeConfigDao {

	ProductTypeConfig get(String id);
	
	List<ProductTypeConfig> listAll();
	
	int insert(ProductTypeConfig productType);
}

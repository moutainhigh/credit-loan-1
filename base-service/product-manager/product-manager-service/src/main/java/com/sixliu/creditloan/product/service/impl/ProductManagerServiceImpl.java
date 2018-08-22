package com.sixliu.creditloan.product.service.impl;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sixliu.credit.common.exception.IllegalArgumentAppException;
import com.sixliu.creditloan.product.dao.ProductConfigDao;
import com.sixliu.creditloan.product.dao.ProductConfigSnapshotDao;
import com.sixliu.creditloan.product.dto.ProductDTO;
import com.sixliu.creditloan.product.entity.ProductConfig;
import com.sixliu.creditloan.product.entity.snapshot.ProductConfigSnapshot;
import com.sixliu.creditloan.product.service.ProductManagerService;

/**
*@author:MG01867
*@date:2018年6月15日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
@RestController
public class ProductManagerServiceImpl implements ProductManagerService{

	final static Logger log = LoggerFactory.getLogger(ProductManagerServiceImpl.class);

	@Autowired
	private ProductConfigDao productConfigDao;
	
	@Autowired
	private ProductConfigSnapshotDao productConfigSnapshotDao;
	
	@Override
	public ProductDTO get(String id) {
		ProductConfig productConfig=productConfigDao.get(id);
		ProductDTO product=null;
		if(null!=productConfig) {
			product=new ProductDTO();
			BeanUtils.copyProperties(productConfig, product);
		}
		return product;
	}



	@Override
	public ProductDTO getByCode(String code) {
		return null;
	}

	@Override
	public String generateProductSnapshot(String productId) {
		ProductConfig productConfig=productConfigDao.get(productId);
		if(null==productConfig) {
			throw new IllegalArgumentAppException(String.format("The product[%s] is illegal", productId));
		}
		ProductConfigSnapshot productConfigSnapshot=new ProductConfigSnapshot();
		BeanUtils.copyProperties(productConfig, productConfigSnapshot);
		productConfigSnapshot.setId(null);
		productConfigSnapshot.setOriginalId(productConfig.getId());
		productConfigSnapshotDao.insert(productConfigSnapshot);
		return productConfigSnapshot.getId();
	}
}
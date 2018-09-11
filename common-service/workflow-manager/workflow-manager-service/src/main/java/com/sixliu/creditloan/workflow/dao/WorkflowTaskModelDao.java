package com.sixliu.creditloan.workflow.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sixliu.creditloan.workflow.entity.WorkflowTaskModel;

/**
*@author:MG01867
*@date:2018年8月13日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface WorkflowTaskModelDao {

	int insert(WorkflowTaskModel workflowTaskModel);
	
	WorkflowTaskModel get(String id);
	
	List<WorkflowTaskModel> listByJobModelId(String jobModelId);
	
	WorkflowTaskModel getByJobModelIdAndPhase(@Param("jobModelId") String jobModelId,@Param("phase") int phase);
}
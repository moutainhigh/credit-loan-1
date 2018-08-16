package com.sixliu.flow.service;

import java.util.Date;

import com.sixliu.flow.JobStatus;
import com.sixliu.flow.TaskStatus;
import com.sixliu.flow.entity.FlowJob;
import com.sixliu.flow.entity.FlowJobModel;
import com.sixliu.flow.entity.FlowTask;
import com.sixliu.flow.entity.FlowTaskModel;

/**
 * @author:MG01867
 * @date:2018年7月23日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class FlowUtils{
	
	public static FlowJob newFlowJob(FlowJobModel flowJobModel,String createUserId) {
		FlowJob flowJob = new FlowJob();
		flowJob.setCreateUserId(createUserId);
		flowJob.setStatus(JobStatus.STARTED);
		flowJob.setUpdateDate(new Date());
		return flowJob;
	}

	public static FlowTask newFlowTask(FlowTaskModel flowTaskModel, String flowJobId,String channelId,
			String userId) {
		FlowTask flowTask = new FlowTask();
		flowTask.setFlowTaskModelId(flowTaskModel.getId());
		flowTask.setFlowJobId(flowJobId);
		flowTask.setPhase(flowTaskModel.getPhase());
		flowTask.setStatus(TaskStatus.POOLING);
		flowTask.setType(flowTaskModel.getType());
		flowTask.setWorker(flowTaskModel.getWorker());
		Date nowDate = new Date();
		flowTask.setStartDate(nowDate);
		flowTask.setEndDate(nowDate);
		flowTask.setChannelId(channelId);
		flowTask.setCreateUserId(userId);
		return flowTask;
	}
}

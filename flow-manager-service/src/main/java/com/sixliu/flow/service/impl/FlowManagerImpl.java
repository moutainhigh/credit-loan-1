package com.sixliu.flow.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.sixliu.credit.user.api.UserManagerClient;
import com.sixliu.flow.ApprovalResult;
import com.sixliu.flow.TaskStatus;
import com.sixliu.flow.approval.AutoApprovalHandler;
import com.sixliu.flow.approval.AutoApprovalHandlerManager;
import com.sixliu.flow.component.FlowStorage;
import com.sixliu.flow.component.FlowTaskSubmitAop;
import com.sixliu.flow.component.FlowTaskSubmitAopManager;
import com.sixliu.flow.dao.FlowJobClassDao;
import com.sixliu.flow.dao.FlowTaskClassDao;
import com.sixliu.flow.entity.FlowInputData;
import com.sixliu.flow.entity.FlowInputDataModel;
import com.sixliu.flow.entity.FlowJob;
import com.sixliu.flow.entity.FlowJobModel;
import com.sixliu.flow.entity.FlowTask;
import com.sixliu.flow.entity.FlowTaskModel;
import com.sixliu.flow.entity.TaskType;
import com.sixliu.flow.service.FlowManager;
import com.sixliu.flow.status.TaskStatusMachine;
import com.sixliu.flow.status.TaskStatusMachineFactory;

/**
 * @author:MG01867
 * @date:2018年7月23日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class FlowManagerImpl implements FlowManager {

	private FlowStorage flowStorage;

	private FlowTaskSubmitAopManager flowTaskSubmitAopManager;
	private AutoApprovalHandlerManager autoApprovalHandlerManager;
	
	@Autowired
	private UserManagerClient userManagerClient;
	
	@Autowired
	private FlowJobClassDao flowJobClassDao;
	
	@Autowired
	private FlowTaskClassDao flowTaskClassDao;
	
	@Autowired
	private TaskStatusMachineFactory taskStatusMachineFactory;

	public FlowManagerImpl(FlowStorage flowStorage,
			FlowTaskSubmitAopManager flowTaskSubmitAopManager, AutoApprovalHandlerManager autoApprovalHandlerManager) {
		this.flowStorage=flowStorage;
		this.flowTaskSubmitAopManager=flowTaskSubmitAopManager;
		this.autoApprovalHandlerManager=autoApprovalHandlerManager;
	}

	@Override
	public void addFlowJobModel(FlowJobModel flowJobModel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFlowTaskModel(FlowTaskModel flowTaskModel) {
		// TODO Auto-generated method stub

	}

	@Override
	public String createFlowJob(String flowJobClassId, String userId, String channel) {
//		FlowJobClass flowJobClass = flowJobClassDao.get(flowJobClassId);
//		if (null == flowJobClass) {
//			throw new IllegalArgumentException(String.format("This flowJobClass[%s] is non-existent", flowJobClassId));
//		}
//		UserDTO user = userManagerClient.get(userId);
//		if (null == user) {
//			throw new IllegalArgumentException(String.format("This user[%s] is non-existent", userId));
//		}
//		String flowJobId = idGenerator.generateFlowJobId();
//		FlowJob flowJob = FlowUtils.newFlowJob(flowJobClass, flowJobId);
//		FlowTaskModel flowTaskModel = flowTaskClassDao.get(flowModelId);
//		if (null == flowTaskModel) {
//			throw new IllegalArgumentException(
//					String.format("This flowTaskModel[%s] configure empty flowTaskModel", flowModelId));
//		}
//		FlowTask flowTask = FlowUtils.newFlowTask(flowTaskModel, flowJobId, idGenerator.generateFlowTaskId(), channel,
//				userId);
//		flowStorage.insertFlowJob(flowJob, flowTask);
//		return flowJobId;
		return null;
	}

	@Override
	public List<FlowTask> listFlowTaskFromPool(String userId) {
		return null;
	}

	@Override
	public void receiveFlowTask(String userId, String taskId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void autoReceiveFlowTask(String userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void additionalInputData(String flowJoblId, String flowTasklId, Map<String, FlowInputData> flowInputDatas) {
		FlowJob flowJob = getAndCheckFlowJob(flowJoblId);
		FlowTask flowTask = getAndCheckFlowTask(flowJoblId, flowTasklId);
		List<FlowInputDataModel> flowInputDataModels = flowStorage.listFlowInputDataModel(flowJob.getFlowJobModelId(),
				flowTask.getFlowTaskModelId());
		if (null == flowInputDataModels || flowInputDataModels.isEmpty()) {
			throw new IllegalArgumentException(String.format(
					"The flowTask[%s] of flowJob[%s] didn't config flowInputDataModel", flowTasklId, flowJoblId));
		}
		if (null == flowInputDatas || flowInputDatas.isEmpty()) {
			throw new IllegalArgumentException(String.format(
					"Please provide inputDatas when the flowTask[%s] of flowJob[%s] config flowInputDataModel",
					flowTasklId, flowJoblId));
		}
		flowInputDataModels.forEach(flowInputDataModel -> {
			flowInputDataModel.getInputName();
			flowInputDatas.get("");
		});

	}

	@Override
	public void submitApprovalResult(ApprovalResult approvalResult) {
		FlowJob flowJob = getAndCheckFlowJob(approvalResult.getFlowJobId());
		FlowTask flowTask = getAndCheckFlowTask(approvalResult.getFlowJobId(), approvalResult.getFlowTaskId());

		FlowTaskSubmitAop flowTaskBeforeSubmitAop = flowTaskSubmitAopManager
				.get(flowTask.getFlowTaskBeforeSubmitAopClass());
		Objects.requireNonNull(flowTaskBeforeSubmitAop,
				String.format("The flowTask[%s] of flowJob[%s] didn't configure flowTaskBeforeSubmitAop",
						flowTask.getId(), flowJob.getId()));
		flowTaskBeforeSubmitAop.intercept(flowTask, approvalResult);

		TaskStatus taskStatus = flowTask.getStatus();
		TaskStatusMachine taskStatusMachine=taskStatusMachineFactory.get(taskStatus);
		FlowTask nextFlowTask = taskStatusMachine.process(flowJob, flowTask, approvalResult);
		flowStorage.updateFlowTask(flowTask);

		FlowTaskSubmitAop flowTaskAfterSubmitAop = flowTaskSubmitAopManager
				.get(flowTask.getFlowTaskAfterSubmitAopClass());
		Objects.requireNonNull(flowTaskAfterSubmitAop,
				String.format("The flowTask[%s] of flowJob[%s] didn't configure flowTaskAfterSubmitAop",
						flowTask.getId(), flowJob.getId()));
		flowTaskAfterSubmitAop.intercept(flowTask, approvalResult);

		if (null != nextFlowTask) {
			flowStorage.insertFlowJob(flowJob, nextFlowTask);
			if (TaskType.AUTO == nextFlowTask.getType()) {
				AutoApprovalHandler autoApprovalHandler = autoApprovalHandlerManager
						.get(nextFlowTask.getWorker());
				autoApprovalHandler.process(nextFlowTask);
			}
		}
	}

	@Override
	public void cancelFlowJob(String jobId) {

	}

	private FlowJob getAndCheckFlowJob(String flowJobId) {
		FlowJob flowJob = flowStorage.getFlowJob(flowJobId);
		if (null == flowJob) {
			throw new IllegalArgumentException(String.format("This flowJob[%s] is non-existent", flowJobId));
		}
		return flowJob;
	}

	private FlowTask getAndCheckFlowTask(String flowJobId, String flowTaskId) {
		FlowTask flowTask = flowStorage.getFlowTask(flowTaskId);
		if (null == flowTask) {
			throw new IllegalArgumentException(
					String.format("This flowTask[%s] of flowJob[%s] is non-existent", flowTaskId, flowJobId));
		}
		return flowTask;
	}
}

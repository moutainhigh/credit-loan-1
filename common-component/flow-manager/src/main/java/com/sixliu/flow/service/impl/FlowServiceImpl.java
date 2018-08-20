package com.sixliu.flow.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sixliu.credit.common.exception.IllegalArgumentAppException;
import com.sixliu.flow.FlowJob;
import com.sixliu.flow.FlowJobModel;
import com.sixliu.flow.FlowTask;
import com.sixliu.flow.FlowTaskModel;
import com.sixliu.flow.FlowTaskResult;
import com.sixliu.flow.JobStatus;
import com.sixliu.flow.TaskStatus;
import com.sixliu.flow.TaskType;
import com.sixliu.flow.User;
import com.sixliu.flow.component.FlowTaskSubmitAop;
import com.sixliu.flow.component.FlowTaskSubmitAopManager;
import com.sixliu.flow.dao.FlowJobDao;
import com.sixliu.flow.dao.FlowJobModelDao;
import com.sixliu.flow.dao.FlowTaskDao;
import com.sixliu.flow.dao.FlowTaskModelDao;
import com.sixliu.flow.dao.UserDao;
import com.sixliu.flow.handler.AutoApprovalHandler;
import com.sixliu.flow.handler.AutoApprovalHandlerManager;
import com.sixliu.flow.service.FlowService;
import com.sixliu.flow.service.FlowUtils;
import com.sixliu.flow.status.TaskStatusMachine;
import com.sixliu.flow.status.TaskStatusMachineFactory;

/**
 * @author:MG01867
 * @date:2018年7月23日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class FlowServiceImpl implements FlowService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private FlowJobModelDao flowJobModelDao;

	@Autowired
	private FlowTaskModelDao flowTaskModelDao;

	@Autowired
	private FlowJobDao flowJobDao;

	@Autowired
	private FlowTaskDao flowTaskDao;

	@Autowired
	private TaskStatusMachineFactory taskStatusMachineFactory;

	@Value("${workerThreads}")
	private int workerThreads;

	@Value("${scheduledThreads}")
	private int scheduledThreads;

	private FlowTaskSubmitAopManager flowTaskSubmitAopManager;

	private AutoApprovalHandlerManager autoApprovalHandlerManager;

	@PostConstruct
	public void init() {
		this.flowTaskSubmitAopManager = new FlowTaskSubmitAopManager();
		this.autoApprovalHandlerManager = new AutoApprovalHandlerManager(workerThreads, scheduledThreads);
	}

	@Override
	public String createFlowJob(String flowJobModelId,String userId) {
		FlowJobModel flowJobModel = flowJobModelDao.get(flowJobModelId);
		if (null == flowJobModel) {
			throw new IllegalArgumentAppException(
					String.format("The flowJobClass[%s] is non-existent", flowJobModelId));
		}
		User user = userDao.get(flowJobModelId);
		if (null == user) {
			throw new IllegalArgumentAppException(
					String.format("The user[%s] is non-existent", userId));
		}
		if (!StringUtils.equals(user.getRoleId(), flowJobModel.getCreateRoleId())) {
			throw new IllegalArgumentAppException(
					String.format("The user[%s] create flowJob of flowJobClass[%s] Permission denied",
							userId, flowJobModel));
		}
		FlowJob flowJob = FlowUtils.newFlowJob(flowJobModel, user.getId());
		flowJobDao.insert(flowJob);
		FlowTaskModel flowTaskClass = flowTaskModelDao.getByflowJobModelAndPhase(flowJobModel.getId(), 0);
		if (null == flowTaskClass) {
			throw new IllegalArgumentException(
					String.format("The flowJobClass[%s] configure empty flowTaskModel", flowJobModel.getId()));
		}
		FlowTask flowTask = FlowUtils.newFlowTask(flowTaskClass, flowJob.getId(),
				user.getId());
		flowTaskDao.insert(flowTask);
		return flowJob.getId();
	}

	@Override
	public List<FlowTask> listFlowTask(String userId){
		User user = getAndCheckUser(userId);
		List<FlowTask> result = flowTaskDao.listByRoleId(user.getRoleId());
		return result;
	}
	
	@Override
	public List<FlowTask> listFlowTask(String userId,TaskStatus status){
		User user = getAndCheckUser(userId);
		List<FlowTask> result = flowTaskDao.listByRoleIdAndStatus(user.getRoleId(),status);
		return result;
	}


	@Override
	public String autoClaimFlowTask(String userId) {
		User user = getAndCheckUser(userId);
		FlowTask claimFlowTask = randomFlowTaskId(user.getId());
		FlowTaskResult flowTaskResult=new FlowTaskResult();
		flowTaskResult.setFlowJobId(claimFlowTask.getFlowJobId());
		flowTaskResult.setFlowTaskId(claimFlowTask.getId());
		flowTaskResult.setStatus(TaskStatus.PENDING);
		flowTaskResult.setUserId(userId);
		submitFlowTask(flowTaskResult);
		return claimFlowTask.getId();
	}
	
	
	@Override
	public void submitFlowTask(FlowTaskResult flowTaskResult) {
		FlowJob flowJob = getAndCheckFlowJob(flowTaskResult.getFlowJobId());
		FlowTask flowTask = getAndCheckFlowTask(flowTaskResult.getFlowJobId());

		FlowTaskSubmitAop flowTaskBeforeSubmitAop = flowTaskSubmitAopManager
				.get(flowTask.getFlowTaskBeforeSubmitAopClass());
		Objects.requireNonNull(flowTaskBeforeSubmitAop,
				String.format("The flowTask[%s] of flowJob[%s] didn't configure flowTaskBeforeSubmitAop",
						flowTask.getId(), flowJob.getId()));
		flowTaskBeforeSubmitAop.intercept(flowTask, flowTaskResult);

		TaskStatus taskStatus = flowTask.getStatus();
		TaskStatusMachine taskStatusMachine = taskStatusMachineFactory.get(taskStatus);
		FlowTask nextFlowTask = taskStatusMachine.process(flowJob, flowTask, flowTaskResult);
		flowTaskDao.update(flowTask);

		FlowTaskSubmitAop flowTaskAfterSubmitAop = flowTaskSubmitAopManager
				.get(flowTask.getFlowTaskAfterSubmitAopClass());
		Objects.requireNonNull(flowTaskAfterSubmitAop,
				String.format("The flowTask[%s] of flowJob[%s] didn't configure flowTaskAfterSubmitAop",
						flowTask.getId(), flowJob.getId()));
		flowTaskAfterSubmitAop.intercept(flowTask, flowTaskResult);

		if (null != nextFlowTask) {
			flowTaskDao.insert(nextFlowTask);
			if (TaskType.AUTO == nextFlowTask.getType()) {
				AutoApprovalHandler autoApprovalHandler = autoApprovalHandlerManager.get(nextFlowTask.getWorker());
				autoApprovalHandler.process(nextFlowTask);
			}
		}
	}

	@Override
	public void cancelFlowJob(String flowJobId, String userId) {
		FlowJob flowJob = getAndCheckFlowJob(flowJobId);
		if (!StringUtils.equals(userId, flowJob.getCreateUserId())) {
			throw new IllegalArgumentAppException(String
					.format("The user[%s] cancelFlowJob flowJob[%s] Permission denied", userId, userId, flowJobId));
		}
		flowJob.setStatus(JobStatus.CANCEL_ENDED);
		flowJob.setUpdateDate(new Date());
	}
	
	private FlowTask randomFlowTaskId(String userId) {
		return null;
	}

	private User getAndCheckUser(String userId) {
		User user = userDao.get(userId);
		if (null == user) {
			throw new IllegalArgumentAppException(String.format("The user[%s] is non-existent", userId));
		}
		return user;
	}
	
	private FlowJob getAndCheckFlowJob(String flowJobId) {
		FlowJob flowJob = flowJobDao.get(flowJobId);
		if (null == flowJob) {
			throw new IllegalArgumentException(String.format("This flowJob[%s] is non-existent", flowJobId));
		}
		return flowJob;
	}

	private FlowTask getAndCheckFlowTask(String flowTaskId) {
		FlowTask flowTask = flowTaskDao.get(flowTaskId);
		if (null == flowTask) {
			throw new IllegalArgumentException(String.format("This flowTask[%s] is non-existent", flowTaskId));
		}
		return flowTask;
	}

}
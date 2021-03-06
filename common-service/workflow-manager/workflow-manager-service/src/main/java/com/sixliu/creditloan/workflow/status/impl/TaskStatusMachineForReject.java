package com.sixliu.creditloan.workflow.status.impl;

import org.springframework.stereotype.Component;

import com.sixliu.creditloan.workflow.entity.WorkflowJob;
import com.sixliu.creditloan.workflow.entity.WorkflowTask;
import com.sixliu.creditloan.workflow.status.AbstractTaskStatusMachine;
import com.sixliu.workflow.constant.TaskStatus;
import com.sixliu.workflow.dto.TaskProcessResult;

/**
*@author:MG01867
*@date:2018年9月7日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
@Component("REJECT")
public class TaskStatusMachineForReject extends AbstractTaskStatusMachine {

	public TaskStatusMachineForReject() {
		super(TaskStatus.REJECT);
	}

	@Override
	protected WorkflowTask doProcess(WorkflowJob workflowJob, WorkflowTask workflowTask,
			TaskProcessResult taskProcessResult) {
		throw new UnsupportedOperationException(
				String.format("No operation is supported when the flowTask[%s]'s status[%s] of flowJob[%s]",
						workflowTask.getId(), TaskStatus.REJECT, workflowJob.getId()));
	}

}

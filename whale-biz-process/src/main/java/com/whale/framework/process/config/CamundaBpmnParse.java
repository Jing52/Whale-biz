package com.whale.framework.process.config;

import com.whale.framework.process.listener.ProcessTaskListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class CamundaBpmnParse extends BpmnParse {
	
	public CamundaBpmnParse(BpmnParser parser) {
		super(parser);
	}
	
	@Override
	protected void parseTaskListeners(Element userTaskElement, ActivityImpl activity, TaskDefinition taskDefinition) {
		super.parseTaskListeners(userTaskElement, activity, taskDefinition);
		TaskListener myTaskListener = new ProcessTaskListener();
		taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, myTaskListener);
		taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, myTaskListener);
		taskDefinition.addTaskListener(TaskListener.EVENTNAME_TIMEOUT, myTaskListener);
	}
	
}

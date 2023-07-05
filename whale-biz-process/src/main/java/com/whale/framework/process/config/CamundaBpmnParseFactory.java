package com.whale.framework.process.config;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.DefaultBpmnParseFactory;

public class CamundaBpmnParseFactory extends DefaultBpmnParseFactory {

	@Override
	public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
		return new CamundaBpmnParse(bpmnParser);
	}
}

package com.whale.framework.process.config;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;

public class CamundaProcessEngineConfiguration extends SpringProcessEngineConfiguration {

	CamundaAuthCommandInterceptor camundaAuthCommandInterceptor;

	public CamundaProcessEngineConfiguration(CamundaAuthCommandInterceptor camundaAuthCommandInterceptor) {
		super();
		this.camundaAuthCommandInterceptor = camundaAuthCommandInterceptor;
	}
	
	@Override
	protected void initActualCommandExecutor() {
		actualCommandExecutor = camundaAuthCommandInterceptor;
	}
	
}

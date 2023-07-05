package com.whale.framework.process.config;

import com.magus.framework.system.api.UserRestApi;
import com.magus.framework.system.api.WorkgroupRestApi;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author: CharlesXiong
 * @date: 2022年3月28日下午2:08:42
 * @notes:
 */
@Configuration
@DependsOn("dynamicDataSourceUtil")
public class CamundaConfig {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	protected UserRestApi userRestApi;

	@Autowired
	protected WorkgroupRestApi workgroupRestApi;

	@Autowired
    CamundaAuthCommandInterceptor camundaAuthCommandInterceptor;

	@Bean
	public ProcessEngineFactoryBean processEngine() {
		ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
		processEngineFactoryBean.setProcessEngineConfiguration(springProcessEngineConfiguration());
		return processEngineFactoryBean;
	}

	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration() {
		CamundaProcessEngineConfiguration configuration = new CamundaProcessEngineConfiguration(camundaAuthCommandInterceptor);
		configuration.setDataSource(dataSource);
		configuration.setTransactionManager(platformTransactionManager);
		configuration.setDbMetricsReporterActivate(false);
		configuration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
		// true 对数据库中所有表进行更新操作。如果表不存在，则自动创建。
		configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		configuration.setBpmnParseFactory(new CamundaBpmnParseFactory());
		configuration.setJdbcBatchProcessing(Boolean.FALSE);
		configuration.setIdGenerator(new StrongUuidGenerator());
		return configuration;
	}

}

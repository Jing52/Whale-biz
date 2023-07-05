package com.whale.framework.process.config;

import com.magus.framework.core.context.HeaderContextHolder;
import com.magus.framework.core.dto.model.BaseUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.config
 * @Description: 用户认证拦截
 * @author Whale
 * @Date: 2022/12/5 11:56 AM
 */

@DependsOn("serviceHeaderInterceptor")
@Component(CamundaAuthCommandInterceptor.BEAN_NAME)
@Slf4j
public class CamundaAuthCommandInterceptor extends CommandInterceptor {

	public static final String BEAN_NAME = "magus::camunda::AuthCommandInterceptor";

	@Autowired
	@Lazy
	IdentityService identityService;

	@Override
	public <T> T execute(Command<T> command) {
		String loginId = null;
		try {
			BaseUser baseUser = HeaderContextHolder.getUser();
			if (Objects.nonNull(baseUser)) {
				loginId = baseUser.getLoginId();
			}
		} catch (Exception e) {
			log.error("===================> Load current user error:", e);
			return command.execute(Context.getCommandContext());
		}

		if (StringUtils.isBlank(loginId)) {
			return command.execute(Context.getCommandContext());
		}
		Authentication currentAuthentication = new Authentication(loginId, null, null);
		identityService.setAuthentication(currentAuthentication);
		// execute
		T execute = command.execute(Context.getCommandContext());
		// clean
		identityService.clearAuthentication();
		return execute;
	}
}

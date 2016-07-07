package com.orange.strategy.app;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.cf.operations.PaaSClient;
import com.orange.model.Application;
import com.orange.model.Step;

public class UpdateProperty extends Step {
	private static final Logger logger = LoggerFactory.getLogger(Delete.class);

	private PaaSClient client;
	private Application application;

	public UpdateProperty(PaaSClient client, Application application) {
		super(String.format("UpdateProperty %s.%s", client.getTargetName(), application.getName()));
		this.client = client;
		this.application = application;
	}

	@Override
	public void exec() {
		logger.info("start {} app: {} on the target: {}", this.getClass().getName(), application.getName(),
				client.getTargetName());
		String appId = client.getAppId(application.getName());
		assert appId != null;
		Map<String, String> env = new HashMap<String, String>();
		env.put("APP_VERSION", application.getVersion());
		client.updateApp(appId, null, env, null);
		logger.info("Step {} Done! App: {} on the target: {}", this.getClass().getName(), application,
				client.getTargetName());
	}

}

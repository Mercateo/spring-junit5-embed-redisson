/**
 * Copyright © 2018 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercateo.spring.junit5.embed.redisson;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.SocketUtils;

import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;

@Slf4j
public class EmbedRedisServer extends SpringExtension implements AfterTestExecutionCallback, ParameterResolver {

	private static final String REDIS_SERVER = EmbedRedisServer.class.getSimpleName();
	private static final String REDIS_SERVER_PORT = REDIS_SERVER + "_port";

	private Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		super.afterTestExecution(context);
		stopRedisServer(context);
	}

	private synchronized void stopRedisServer(ExtensionContext context) {
		Store store = getStore(context);

		Integer port = (Integer) store.remove(REDIS_SERVER_PORT);
		RedisServer server = (RedisServer) store.remove(REDIS_SERVER);
		if (server != null)
		{
			log.info("Stopping Redis server on port "+port);
			server.stop();
		}
			
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return super.supportsParameter(parameterContext, extensionContext) || isRedisson(parameterContext);
	}

	private boolean isRedisson(ParameterContext parameterContext) {
		return parameterContext.getParameter().getType().isAssignableFrom(RedissonClient.class);
	}

	@Override
	public synchronized Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		if (isRedisson(parameterContext)) {

			int port = startRedisServer(extensionContext);

			Config config = new Config();
			config.useSingleServer().setAddress("redis://localhost:" + port);
			RedissonClient c = Redisson.create(config);
			
			return c;
		} else
			return super.resolveParameter(parameterContext, extensionContext);
	}

	private int startRedisServer(ExtensionContext context) {
		Store store = getStore(context);

		Integer port = (Integer) store.get(REDIS_SERVER_PORT);
		if (port != null)
			return port;

		port = SocketUtils.findAvailableTcpPort();
		log.info("Starting Redis server on port "+port);
		RedisServer redisServer = new RedisServer(port);
		redisServer.start();

		store.put(REDIS_SERVER, redisServer);
		store.put(REDIS_SERVER_PORT, port);
		
		return port;
	}

}

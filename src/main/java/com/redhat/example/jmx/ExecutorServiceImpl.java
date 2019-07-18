package com.redhat.example.jmx;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.util.concurrent.BlockingTaskAwareExecutorServiceImpl;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

public class ExecutorServiceImpl implements ExecutorServiceMXBean {
	static Log log = LogFactory.getLog(ExecutorServiceImpl.class);

	EmbeddedCacheManager manager;
	String name;

	public ExecutorServiceImpl(EmbeddedCacheManager manager, String name) {
		this.manager = manager;
		this.name = name;
	}

	@Override
	public int getpoolSize() {
		ExecutorService executor = manager.getGlobalComponentRegistry().getComponent(ExecutorService.class, name);
		ExecutorService delegate = getDelegate(executor);
		if (delegate instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) delegate).getPoolSize();
		} else {
			return -1;
		}
	}

	@Override
	public int getactiveCount() {
		ExecutorService executor = manager.getGlobalComponentRegistry().getComponent(ExecutorService.class, name);
		ExecutorService delegate = getDelegate(executor);
		if (delegate instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) delegate).getActiveCount();
		} else {
			return -1;
		}
	}

	@Override
	public int getmaximumPoolSize() {
		ExecutorService executor = manager.getGlobalComponentRegistry().getComponent(ExecutorService.class, name);
		ExecutorService delegate = getDelegate(executor);
		if (delegate instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) delegate).getMaximumPoolSize();
		} else {
			return -1;
		}
	}

	@Override
	public int getlargestPoolSize() {
		ExecutorService executor = manager.getGlobalComponentRegistry().getComponent(ExecutorService.class, name);
		ExecutorService delegate = getDelegate(executor);
		if (delegate instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) delegate).getLargestPoolSize();
		} else {
			return -1;
		}
	}

	@Override
	public int getqueueSize() {
		ExecutorService executor = manager.getGlobalComponentRegistry().getComponent(ExecutorService.class, name);
		ExecutorService delegate = getDelegate(executor);
		if (delegate instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) delegate).getQueue().size();
		} else {
			return -1;
		}
	}

	@Override
	public long getkeepAliveTime() {
		ExecutorService executor = manager.getGlobalComponentRegistry().getComponent(ExecutorService.class, name);
		ExecutorService delegate = getDelegate(executor);
		if (delegate instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) delegate).getKeepAliveTime(TimeUnit.MILLISECONDS);
		} else {
			return -1;
		}
	}

	public ExecutorService getDelegate(ExecutorService executor) {
		try {
			Field field = executor.getClass().getDeclaredField("delegate");
			field.setAccessible(true);
			executor = (ExecutorService) field.get(executor);
			if (executor instanceof BlockingTaskAwareExecutorServiceImpl) {
				field = executor.getClass().getDeclaredField("executorService");
				field.setAccessible(true);
				executor = (ExecutorService) field.get(executor);
			}
			return executor;
		} catch (Exception e) {
			return null;
		}
	}
}

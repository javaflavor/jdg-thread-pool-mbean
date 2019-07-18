package com.redhat.example.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.infinispan.factories.KnownComponentNames;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

public class MXBeanFactory {
	static Log log = LogFactory.getLog(MXBeanFactory.class);

	static final String MBEAN_NAME = "com.redhat.example:type=CacheManager,name=\"%s\",component=%s";

	static final String COMPONENT_NAMES[] = {
			KnownComponentNames.ASYNC_TRANSPORT_EXECUTOR,
			KnownComponentNames.REMOTE_COMMAND_EXECUTOR,
			KnownComponentNames.ASYNC_NOTIFICATION_EXECUTOR,
			KnownComponentNames.PERSISTENCE_EXECUTOR,
			KnownComponentNames.EXPIRATION_SCHEDULED_EXECUTOR,
			KnownComponentNames.STATE_TRANSFER_EXECUTOR,
			KnownComponentNames.ASYNC_OPERATIONS_EXECUTOR,
			KnownComponentNames.TIMEOUT_SCHEDULE_EXECUTOR
	};

	MBeanServer mbeanServer;
	List<ObjectName> registered = new ArrayList<>();

	ExecutorServiceImpl rollingUpgradeManagerEx;

	public void registerMBean(EmbeddedCacheManager manager) {
		try {
			mbeanServer = ManagementFactory.getPlatformMBeanServer();
			String clusterName = manager.getClusterName();

			for (String componentName : COMPONENT_NAMES) {
				ExecutorService executor = manager.getGlobalComponentRegistry().getComponent(ExecutorService.class,
						componentName);
				if (executor != null) {
					ExecutorServiceImpl mbean = new ExecutorServiceImpl(manager, componentName);
					ObjectName objectName = new ObjectName(String.format(MBEAN_NAME, clusterName, componentName));
					mbeanServer.registerMBean(mbean, objectName);
					registered.add(objectName);
					log.infof("### ExecutorService MBean registered: %s", objectName);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to register monitoring MBean.", e);
		}
	}

	@PreDestroy
	public void unregisterMBean() {
		try {
			for (ObjectName objectName: registered) {
				mbeanServer.unregisterMBean(objectName);
				log.infof("### Custom MBean unregistered: %s", objectName);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to unregister monitoring MBean.", e);
		}
	}
}

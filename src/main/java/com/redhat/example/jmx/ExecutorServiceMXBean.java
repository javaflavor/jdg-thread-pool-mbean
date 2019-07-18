package com.redhat.example.jmx;

public interface ExecutorServiceMXBean {
	
	public int getpoolSize();

	public int getactiveCount();

	public int getmaximumPoolSize();

	public int getlargestPoolSize();

	public int getqueueSize();

	public long getkeepAliveTime();
}

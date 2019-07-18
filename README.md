# Custom Thread Pool MBean for RHDG 7.2

## How to use

### Preparation

Apache Maven is used to build the tool of the project. Check if JDK 8 and Apache Maven are available in your environment.

~~~
$  mvn --version
Apache Maven 3.5.4 (1edded0938998edf8bf061f1ceb3cfdeccf443fe; 2018-06-18T03:33:14+09:00)
		:
$ javac -version
javac 1.8.0_151
~~~

### Build the deployment module

The module can be built by the following command.

~~~
$ cd jdg-thread-pool-mbean

$ mvn clean package
~~~

Check "BUILD SUCCESS" message.

~~~
	:
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.258 s
[INFO] Finished at: 2019-07-18T18:52:59+09:00
[INFO] ------------------------------------------------------------------------
~~~

The deployable module can be found in the target directory.

~~~
$ ls target/
./                              generated-sources/              maven-status/
../                             jdg-thread-pool-mbean.jar
classes/                        maven-archiver/
~~~
    
### Deply the module in JDG server

Copy the created module jdg-thread-pool-mbean.jar into the deployments directory of all JDG instances in the cluster.

~~~
$ scp target/jdg-thread-pool-mbean.jar \
    jboss@server1:/opt/jboss/jboss-datagrid-7.2.0-server/node1/deployments/
$ scp target/jdg-thread-pool-mbean.jar \
    jboss@server2:/opt/jboss/jboss-datagrid-7.2.0-server/node2/deployments/
    :
~~~

Create a dummy cache, called `executorServiceCache` using the deployed module and restart the server.

The CLI script `add-executor-service-cache.cli` is available for creating cache.

~~~
$ /opt/jboss/jboss-datagrid-7.2.0-server/bin/cli.sh \
	--controller='server1:9990' \
	--file=add-executor-service-cache.cli
$ /opt/jboss/jboss-datagrid-7.2.0-server/bin/cli.sh \
	--controller='server2:9990' \
	--file=add-executor-service-cache.cli
	:
~~~

Please check the configuration file clustered.xml is modified as follows:

~~~
                <distributed-cache name="executorServiceCache" mode="SYNC" start="EAGER">
                    <store class="com.redhat.example.jdg.store.ExecutorServiceStore"/>
                </distributed-cache>
~~~

### Test Fright

The module adds Custom MBeans for Infinispan-level thread pools in the Data Grid nodes. The MBeans are exposed as the following name:

~~~
com.redhat.example:type=CacheManager,name="clustered",component=org.infinispan.executors.*
~~~

In case of using jmxstat, you can use the following configuration to collect metrics of all available thread pools.

~~~
var metrics = [
    { mbean : 'com.redhat.example:type=CacheManager,name="clustered",component=org.infinispan.executors.*',
      attrs : [ "activeCount", "poolSize", "largestPoolSize", "queueSize" ]
    }
]
~~~

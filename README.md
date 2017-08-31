# a test application to analyze jvm & OS memory


## to run the application in the docker
build project 
```
gradle build
```
build image with tag 'jvm-memory' run the command in the root path of this project
```
docker build -t jvm-memory
```
get <YOUR HOST MACHINE IP> by ipconfig/ifconfig command. it is th IP of Ethernet adapter vEthernet (DockerNAT):
```
ipconfig

Windows IP Configuration


Ethernet adapter vEthernet (DockerNAT):

   Connection-specific DNS Suffix  . :
   IPv4 Address. . . . . . . . . . . : <YOUR HOST MACHINE IP>
   Subnet Mask . . . . . . . . . . . : 255.255.255.0
   Default Gateway . . . . . . . . . :
...
```
start a docker container 'jvm' with the image 'jvm-memory'
```
# --cap-add=SYS_PTRACE will enable your using jmap jinfo. otherwise you will get
# Error attaching to process: sun.jvm.hotspot.debugger.DebuggerException: Can't attach to the process
docker run --cap-add=SYS_PTRACE --rm -it -p <YOUR HOST MACHINE IP>:9090:9090 --name jvm jvm-memory
```
in the docker bash, start the java main
```
# those -D arguments will enable you connecting the application with the jmx in the hosting machine that run this docker container
# the -XX:NativeMemoryTracking=detail will enable the NMT https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr007.html#BABIIIAC 
# the java application is very easy to use. input number to increase heap memory. input 'release' to release some memory.
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.local.only=false \
     -Dcom.sun.management.jmxremote.port=9090 \
     -Dcom.sun.management.jmxremote.rmi.port=9090 \
     -Djava.rmi.server.hostname=<YOUR HOST MACHINE IP> \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -XX:NativeMemoryTracking=detail \
     -Xms128m -Xmx256m -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=70 -XX:+UseG1GC \
     -cp build/libs/jvm-memory-analysis-1.0.jar org.roger.Memory
```
connect the jvm from the hosting machine locally with the tool visiualvm/jconsole with the connection string
```
service:jmx:rmi:///jndi/rmi://<YOUR HOST MACHINE IP>:9090/jmxrmi
```
attach to the docker container with a separate session
```
# the 'jvm' is the name of the docker container you assigned when starting it
docker exec -it jvm /bin/bash
```
run top to see the java process memory status
```
top
top - 10:26:54 up 14 days, 49 min,  0 users,  load average: 0.16, 0.03, 0.01
Tasks:   4 total,   1 running,   3 sleeping,   0 stopped,   0 zombie
%Cpu(s):  0.2 us,  0.8 sy,  0.0 ni, 98.2 id,  0.0 wa,  0.0 hi,  0.8 si,  0.0 st
KiB Mem:   2035228 total,  1428192 used,   607036 free,   254712 buffers
KiB Swap:  1048572 total,        0 used,  1048572 free.   775240 cached Mem

  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND
    5 root      20   0 2733908 144668  16436 S   0.7  7.1   0:00.60 java
    1 root      20   0   21948   3616   3124 S   0.0  0.2   0:00.03 bash
   26 root      20   0   21944   3664   3168 S   0.0  0.2   0:00.02 bash
   30 root      20   0   23608   2668   2348 R   0.0  0.1   0:00.00 top

```
run jmap to see the jvm memory status.
```
jmap -heap <java process id>
Heap Configuration:
   MinHeapFreeRatio         = 20
   MaxHeapFreeRatio         = 70
   ...
```
run jinfo to change the configuration on-the-fly
```
jinfo -flag MaxHeapFreeRatio=50 <java process id>
```
run jcmd  to see the native memory allocation
```
jcmd <java process id> VM.native_memory summary
```
or use jcmd to set up a memory allocation baseline then do diff
```
jcmd <java process id> VM.native_memory baseline
jcmd <java process id> VM.native_memory summary.diff
```
or use together with the system command pmap to bridge the memory allocation from system view and jvm view
```
jcmd <java process id> VM.native_memory detail
pmap -x <java process id>
```
#!/bin/bash
CLASSPATH="/Users/vmarquez/BlueScale/lib/jetty/jetty-6.1.0.jar:/Users/vmarquez/BlueScale/lib/jetty/jetty-util-6.1.0.jar:/Users/vmarquez/BlueScale/lib/jetty/servlet-api-2.5.jar:/Users/vmarquez/BlueScale/lib/jmf.jar:/Users/vmarquez/BlueScale/lib/js.jar:/Users/vmarquez/BlueScale/lib/junit-4.8.2.jar:/Users/vmarquez/BlueScale/lib/log4j-1.2.8.jar:/Users/vmarquez/BlueScale/lib/sip/jain-sip-api-1.2.jar:/Users/vmarquez/BlueScale/lib/sip/jain-sip-ri-1.2.108.jar:/Users/vmarquez/BlueScale/lib/sip/jain-sip-sdp-1.2.108.jar:/Users/vmarquez/BlueScale/resources:/Users/vmarquez/BlueScale/bin:/Users/vmarquez/Dropbox/JavaTools/scala-2.8.0.final/lib/scala-compiler.jar:/Users/vmarquez/Dropbox/JavaTools/scala-2.8.0.final/lib/scala-library.jar"
jvdb -c $CLASSPATH -s "src:test" com.bss.telco.jainsip.unittest.JoinTwoConnectedFunctionalTest "stop in com.bss.telco.jainsip.unittest.JoinTwoConnectedFunctionalTest$.main"
 

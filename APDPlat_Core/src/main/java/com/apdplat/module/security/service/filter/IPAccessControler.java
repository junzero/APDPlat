/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apdplat.module.security.service.filter;

import com.apdplat.platform.log.APDPlatLogger;
import com.apdplat.platform.util.FileUtils;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 *IP地址访问限制
 * @author ysc
 */
public class IPAccessControler {
    protected static final APDPlatLogger log = new APDPlatLogger(IPAccessControler.class);
    
    private Collection<String> allow;
    private Collection<String> deny;

    public IPAccessControler() {
        reInit();
    }
    public final void reInit(){
        allow=FileUtils.getTextFileContent("/WEB-INF/ip/allow.txt");
        deny=FileUtils.getTextFileContent("/WEB-INF/ip/deny.txt");
    }

    public boolean deny(HttpServletRequest request){
        if(request==null){
            return false;
        }
        try{
            String ip = getIpAddr(request);
            if(ip==null){
                log.info("无法获取到访问者的IP");
                return true;
            }

            if (hasMatch(ip, deny)) {
                log.info("ip: "+ip+" 位于黑名单中");
                return true;
            }

            if (!allow.isEmpty() && !hasMatch(ip, allow)) {
                log.info("ip: "+ip+" 没有位于白名单中");
                return true;
            }
        }catch(Exception e){}
        return false;
    }

    private boolean hasMatch(String ip, Collection<String> regExps) {
        for (String regExp : regExps) {
            try{
                if (ip.matches(regExp)) {
                    return true;
                }
            }catch(Exception e){}
        }

        return false;
    }

    private String getIpAddr(HttpServletRequest request){       
        String ipString=null;
        String temp = request.getHeader("x-forwarded-for");
        if(temp.indexOf(":")==-1 && temp.indexOf(".")!=-1){
            ipString=temp;
        }
        if (StringUtils.isBlank(ipString) || "unknown".equalsIgnoreCase(ipString)) {
            temp = request.getHeader("Proxy-Client-IP");
            if(temp.indexOf(":")==-1 && temp.indexOf(".")!=-1){
                ipString=temp;
            }
        }
        if (StringUtils.isBlank(ipString) || "unknown".equalsIgnoreCase(ipString)) {
            temp = request.getHeader("WL-Proxy-Client-IP");
            if(temp.indexOf(":")==-1 && temp.indexOf(".")!=-1){
                ipString=temp;
            }
        }
        if (StringUtils.isBlank(ipString) || "unknown".equalsIgnoreCase(ipString)) {
            temp = request.getRemoteAddr();
            if(temp.indexOf(":")==-1 && temp.indexOf(".")!=-1){
                ipString=temp;
            }
        }

        // 多个路由时，取第一个非unknown的ip
        final String[] arr = ipString.split(",");
        for (final String str : arr) {
            if (!"unknown".equalsIgnoreCase(str) && str.indexOf(":")==-1 && str.split(".").length==4) {
                ipString = str;
                break;
            }
        }

        return ipString;
    }
    public static void main(String[] args){
        System.out.println("127.0.0.1".matches("127.0.*.*"));
    }
}
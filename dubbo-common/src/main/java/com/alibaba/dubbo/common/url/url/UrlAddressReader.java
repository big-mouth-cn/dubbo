/*
 * Copyright 2016 big-mouth.cn
 *
 * The Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.dubbo.common.url.url;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.url.AddressReader;
import com.alibaba.dubbo.common.utils.StringUtils;


/**
 * 从可访问的URL中读取地址
 * <pre>
 * e.g.
 * 
 * ----- Code -----
 * AddressReader reader = new UrlAddressReader("http://www.big-mouth.cn/zkaddrs");
 * System.out.println(reader.read());
 * 
 * ----- Result -----
 * "192.168.1.100:2181,192.168.1.101:2181,192.168.1.102:2181"
 * </pre>
 * @author Allen Hu 
 * 2016-3-17
 */
public class UrlAddressReader implements AddressReader {

    private static final Logger logger = LoggerFactory.getLogger(UrlAddressReader.class);
    public static final String REGX = "(http|https):\\/\\/.*";
    private static final char SPLIT_CHAR = ',';
    private final String url;
    
    public UrlAddressReader(String url) {
        if (StringUtils.isBlank(url))
            throw new IllegalArgumentException("url");
        if (!url.matches(REGX)) {
            throw new IllegalArgumentException("Invalid url! " + url);
        }
        this.url = url;
    }

    @Override
    public String read() {
        String[] urls = url.indexOf(SPLIT_CHAR) > -1 ? StringUtils.split(url, SPLIT_CHAR) : new String[] {url};
        String addrs = null;
        for (String url : urls) {
            addrs = read(url);
            if (StringUtils.isNotEmpty(addrs))
                break;
        }
        if (StringUtils.isBlank(addrs)) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Cannot get content from %s !", url));
            }
            return null;
        }
        return addrs.trim();
    }

    private String read(String url) {
        try {
            byte[] body = get(url);
            String addrs = new String(body);
            return StringUtils.isBlank(addrs) ? null : addrs.trim();
        }
        catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage());
            }
            return null;
        }
    }

    private byte[] get(String url) throws IOException {
        URLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream out = null;
        try {
            URL url2 = new URL(url);
            connection = url2.openConnection();
            connection.connect();
            is = connection.getInputStream();
            out = new ByteArrayOutputStream(is.available());
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = is.read(b)) != -1) {
                out.write(b, 0, len);
            }
            out.flush();
            byte[] body = out.toByteArray();
            return body;
        }
        finally {
            if (null != out) {
                try {
                    out.close();
                }
                catch (IOException e) {
                }
            }
            if (null != is) {
                try {
                    is.close();
                }
                catch (IOException e) {
                }
            }
        }
    }
}

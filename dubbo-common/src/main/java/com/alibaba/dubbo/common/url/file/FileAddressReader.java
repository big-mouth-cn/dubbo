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
package com.alibaba.dubbo.common.url.file;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.url.AddressReader;
import com.alibaba.dubbo.common.url.ReaderException;
import com.alibaba.dubbo.common.utils.IOUtils;
import com.alibaba.dubbo.common.utils.StringUtils;

/**
 * 从文件中读取地址
 * 
 * <pre>
 * e.g.
 * 
 * 第一种方式：指定路径（路径必须以file:开头）
 * ----- Code -----
 * AddressReader reader = new FileAddressReader("file:/opt/bigmouth/zkaddrs.cfg");
 * System.out.println(reader.read());
 * 
 * ----- Result -----
 * "192.168.1.100:2181,192.168.1.101:2181,192.168.1.102:2181"
 * 
 * 第二种方式：默认路径（"%USER_HOME%/url-address"）
 * ----- Code -----
 * AddressReader reader = new FileAddressReader();
 * System.out.println(reader.read());
 * 
 * ----- Result -----
 * "192.168.1.100:2181,192.168.1.101:2181,192.168.1.102:2181"
 * </pre>
 * 
 * @author Allen Hu 2016-3-17
 */
public class FileAddressReader implements AddressReader {

    private static final Logger logger = LoggerFactory.getLogger(FileAddressReader.class);
    private static final String PREFIX = "file:";
    public static final String PREFIX_REGX = PREFIX + ".*";
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String DEFAULT_FILENAME = "url-address";
    
    private final String path;

    public FileAddressReader() {
        this(null);
    }

    public FileAddressReader(String path) {
        if (StringUtils.isNotEmpty(path) && !path.matches(PREFIX_REGX)) {
            throw new RuntimeException("The file path must be begin with '" + PREFIX + "'");
        }
        this.path = path;
    }

    @Override
    public String read() throws ReaderException {
        String filename = path;
        if (StringUtils.isBlank(filename)) {
            String home = USER_HOME;
            if (!USER_HOME.endsWith(File.separator)) {
                home = USER_HOME + File.separator;
            }
            filename = StringUtils.join(new String[] { home, DEFAULT_FILENAME });
        }
        try {
            filename = StringUtils.removeStart(filename, PREFIX);
            String addrs = IOUtils.read(new FileReader(new File(filename)));
            if (StringUtils.isBlank(addrs)) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Cannot get content from file:%s", filename));
                }
                return null;
            }
            return addrs.trim();
        }
        catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage());
            }
            return null;
        }
    }
}

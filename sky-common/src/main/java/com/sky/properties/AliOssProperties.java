package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.alioss")
//用该注解将yml里的外部配置文件（如application.properties或
// application.yml）中的属性绑定到一个Java类上。这样可以方便地在应用程序中使用这些属性
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

}

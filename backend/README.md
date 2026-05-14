# 后端代码规范

https://alidocs.dingtalk.com/i/nodes/k2wz1jPpZ30WoMrNZGrmJNnvrL4A6dxE

# 一些特殊配置

## 定时任务线程池

- spring.task.scheduling.pool.size: 8

## 上传文件大小

- spring.servlet.multipart.max-file-size: 100MB
- spring.servlet.multipart.max-request-size: 100MB

## 关闭swagger-ui

- springdoc.swagger-ui.enabled: false
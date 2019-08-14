# cicco-graceful-shutdown
SpringBoot优雅关闭

### 使用方式
- 添加配置
>  management.endpoints.web.exposure.include=graceful-shutdown #暴露EndPoint <br>
>  graceful.timeout.request=10000      #关闭时请求超时时间 单位:毫秒 <br>
>  graceful.timeout.container=30000     #关闭时容器超时时间 单位:毫秒(最好大于请求超时时间)<br>

- 使用
> curl -X POST localhost:8080/actuator/graceful-shutdown

### 说明
> 使用Filter监听当前请求数量, 当需要关闭应用程序时, 启动关闭线程, 并拒绝新进入的请求(返回503)<br>
> 当请求超过规定的时间(graceful.timeout.graceful配置项)依旧未处理完时, 强制断开链接<br>
> 当运行容器超过规定的时间(graceful.timeout.container配置项)依旧未结束时, 强制关闭应用程序<br>
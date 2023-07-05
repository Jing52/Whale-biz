# 配置
## 钉钉配置
* 第一步：创建钉钉群 并在群中添加自定义机器人

对于不太了解钉钉机器人配置的同学可以参考：钉钉机器人

具体的也可以参考这篇博客: 钉钉机器人SDK 封装预警消息发送工具

* 第二步：增加配置文件

以下以yml配置文件的配置方式为例

```yaml
notice:
  exception:
    enable: 启用开关 false或不配置的话本项目不会生效
    projectName: 指定异常信息中的项目名，不填的话默认取 spring.application.name的值
    included-trace-package: 追踪信息的包含的包名，配置之后只通知此包下的异常信息
    period: 异常信息发送的时间周期 以秒为单位 默认值5，异常信息通知并不是立即发送的，默认设置了5s的周期
    exclude-exceptions:
      - 需要排除的异常通知，注意 这里是异常类的全路径，可多选
    ## 钉钉配置
    ding-talk:
      web-hook: 钉钉机器人的webHook地址，可依次点击钉钉软件的头像，机器人管理，选中机器人来查看
      at-mobiles: 
        - 钉钉机器人发送通知时 需要@的钉钉用户账户，可多选
      msg-type: 消息文本类型 目前支持 text markdown action_card feed_card
```

## 企业微信配置
* 第一步：创建企业微信群 并在群中添加自定义机器人

对于不太了解企业微信机器人配置的同学可以参考：企业微信机器人

* 第二步：增加配置文件

以下以yml配置文件的配置方式为例

```yaml
exception:
  notice:
    enable: 启用开关 false或不配置的话本项目不会生效
    projectName: 指定异常信息中的项目名，不填的话默认取 spring.application.name的值
    included-trace-package: 追踪信息的包含的包名，配置之后只通知此包下的异常信息
    period: 异常信息发送的时间周期 以秒为单位 默认值5，异常信息通知并不是立即发送的，默认设置了5s的周期
    exclude-exceptions:
      - 需要排除的异常通知，注意 这里是异常类的全路径，可多选
    ## 企业微信配置
    wx-work:
      web-hook: 企业微信webhook地址
      at-phones: 手机号列表，提醒手机号对应的群成员(@某个成员)，@all表示提醒所有人 当msg-type=text时才会生效
      at-user-ids: userid的列表，提醒群中的指定成员(@某个成员)，@all表示提醒所有人 当msg-type=text时才会生效
      msg-type: 消息格式 企业微信支持 （text）、markdown（markdown）、图片（image）、图文（news）四种消息类型 本项目中有 text和markdown两种可选
```
## 邮箱配置
这里以qq邮箱为例

* 第一步：项目中引入邮箱相关依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```
* 第二步：增加配置文件
```yaml
exception:
  notice:
    enable: 启用开关 false或不配置的话本项目不会生效
    projectName: 指定异常信息中的项目名，不填的话默认取 spring.application.name的值
    included-trace-package: 追踪信息的包含的包名，配置之后只通知此包下的异常信息
    period: 异常信息发送的时间周期 以秒为单位 默认值5，异常信息通知并不是立即发送的，默认设置了5s的周期，主要为了防止异常过多通知刷屏
    exclude-exceptions:
      - 需要排除的异常通知，注意 这里是异常类的全路径，可多选
    ## 邮箱配置
    mail:
      from: 发送人地址
      to: 接收人地址
      cc: 抄送人地址
spring:
 mail:
   host: smtp.qq.com  邮箱server地址 
   username: 4545545@qq.com  server端发送人邮箱地址
   password: 邮箱授权码
```
邮箱授权码可以按以下方法获取

打开 QQ邮箱网页→设置→账户→POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务→开启POP3/SMTP服务，然后就能看到授权码了

注意：钉钉,企业微信和邮箱配置支持单独和同时启用
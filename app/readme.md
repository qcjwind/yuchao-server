
## 启动、重启（会自动关闭已启动的应用，然后再启动）
1. 将jar、yml、shell文件放在同一目录中
2. 确认已安装jdk8
3. 运行 start.sh


## 日志路径配置
application-out.yml 中可设置日志路径 

## 健康检查
curl http://127.0.0.1:11888/healthCheck

## swagger
http://127.0.0.1:11888/swagger-ui.html



每分钟健康检查一次
```
0 2 * * * /Users/henry/radio/start.sh
```


https://pixso.cn/app/design/74lPRx3BZ3JHaVz5Xkiz_A?page-id=1%3A2 邀请您加入 Pixso 设计文件「购票APP 原型」， 密码：8CmD


scp /Users/henry/IdeaProjects/yuchao/app/target/yuchao.jar root@47.108.163.156:/Users/henry/yuchao/

ssh root@47.108.163.156 '/Users/henry/yuchao/start.sh'


账号：重庆中升智联科技有限公司
密码：Zszl123456


## 获取accessToken

https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx5c89ad708794b0bc&secret=e32a03c306f34d5b2fbd30fc1fbe8ef9

## 消息调试
https://developers.weixin.qq.com/apiExplorer?type=messagePush





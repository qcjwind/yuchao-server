#!/bin/bash

## 进入shell所在目录
## https://www.cnblogs.com/xuxm2007/p/7554543.html
cd `dirname $0`

#response=`curl -s "http://127.0.0.1:11888/healthCheck"`
#
#if (response) {
#
#}

if curl -fs http://127.0.0.1:11888/healthCheck; then
    echo "请求成功"
else
    echo "请求失败"
    ./start.sh
fi

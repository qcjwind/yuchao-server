#!/bin/bash

## 进入shell所在目录
## https://www.cnblogs.com/xuxm2007/p/7554543.html
cd `dirname $0`

appName=yuchao

backupDir="./bak"

# 使用ps命令查找Java进程的ID
pid=$(ps aux | grep 'java' | grep 'jar' | grep ${appName} | awk '{print $2}')

# 判断pid是否为空
if [ -z "$pid" ]; then
  echo "No Java process found"
else
  echo "Java process ID: $pid"

  # 使用kill命令终止Java进程
  kill -9 $pid

  echo "Java process terminated"
fi

# 2. 创建备份目录（如果不存在）
if [ ! -d "$backupDir" ]; then
  mkdir -p "$backupDir"
  echo "已创建备份目录：$backupDir"
fi

# 3. 备份当前jar文件（带时间戳，格式：应用名.jar_20251028163045）
timestamp=$(date +%Y%m%d_%H%M%S)  # 时间戳：年月日时分秒
cp "${appName}.jar" "${backupDir}/${appName}.jar_${timestamp}"

# 直接删除：风险较低，适合确认文件无误的场景
find "$backupDir" -type f -name "*jar_*" -mtime +30 -delete

echo "Start ${appName}.jar"

nohup java -Dloader.path="libs/" -jar ${appName}.jar --spring.config.additional-location=application-out.yml > info.log 2>&1 &


#!/bin/bash

## 进入shell所在目录
## https://www.cnblogs.com/xuxm2007/p/7554543.html
cd `dirname $0`

# 配置参数：指定需要监控的固定文件（可填写多个，用空格分隔）
FILES=(
  "/path/to/your/fixed/log1.log"   # 第一个固定文件路径
  "/path/to/your/fixed/log2.log"   # 第二个固定文件路径（可选，可删除）
)
WEBHOOK="https://oapi.dingtalk.com/robot/send?access_token=你的token"  # 钉钉Webhook

# 计算3小时前的时间戳（秒级）
three_hours_ago=$(( $(date +%s) - 3*3600 ))

# 存储3小时内有修改的文件
updated_files=()

# 遍历每个固定文件，检查修改时间
for file in "${FILES[@]}"; do
  # 检查文件是否存在
  if [ ! -f "$file" ]; then
    echo "警告：文件 $file 不存在，跳过检查"
    continue
  fi

  # 获取文件最后修改时间戳（秒级）
  # stat命令兼容性处理：Linux用%Y，macOS用%m
  if [[ "$(uname)" == "Linux" ]]; then
    modify_ts=$(stat -c %Y "$file")
  else
    modify_ts=$(stat -f %m "$file")
  fi

  # 判断是否在3小时内有修改
  if [ "$modify_ts" -ge "$three_hours_ago" ]; then
    updated_files+=("$file")
  fi
done

if [ ${#updated_files[@]} -gt 0 ]; then

  # Markdown 内容（支持标题、列表、链接等，需包含关键词）
  MARKDOWN_CONTENT='# 【告警通知】请登录服务器查看'

  # 发送 Markdown 消息
  MESSAGE='{
      "msgtype": "markdown",
      "markdown": {
          "title": "服务器告警",
          "text": "'"$MARKDOWN_CONTENT"'"
      }
  }'

  curl -X POST \
    -H "Content-Type: application/json;charset=UTF-8" \
    -d "$MESSAGE" \
    "$WEBHOOK"

  echo "告警已发送，更新的文件：${updated_files[*]}"
else
  echo "最近3小时内，指定的固定文件均无新增内容"
fi

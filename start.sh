#!/bin/zsh

# Cloudflare R2配置
export CLOUDFLARE_R2_ACCESS_KEY_ID=f1922346cfc60a654e3f25b7210a691c
export CLOUDFLARE_R2_ACCESS_KEY_SECRET=8e3766e53878eea0935c9426089da45ed4bdd72bf22301c22fb24090d3be1223

# 邮件配置
export MAIL_USERNAME=noneedtofan@qq.com
export MAIL_PASSWORD=ztatcjfydfgceadh
export MAIL_PORT=465

# MySQL配置
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=XMTXyyds2025

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=XMTXyyds2025

# 启动应用
java -jar game9-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod 
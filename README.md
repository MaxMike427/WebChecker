# WebChecker

将一个特定的网页变为应用的工具
WebChecker 是一个基于 Android WebView 的轻量级浏览器应用，专为便捷的网页浏览而设计。它支持 HTTP/HTTPS 协议，具有自动协议补全功能，并针对视频播放进行了优化。(注意某国内搜索引擎可能无法正常使用)

## V1.3 (当前版本)

### 更新日期
2025-08-15

### 主要改进
1. 改进了视频播放体验

## 产品生成
Trae"https://www.trae.cn/"

AI调用"Doubao-seed-1.6,Qwen-3-Coder,GLM-4.5"

Android Studio"https://developer.android.google.cn/studio?hl=zh-cn"

Gradle"https://gradle.org/"

Java-17"https://www.java.com/zh-CN/"

## 功能特性

- **智能 URL 输入**：自动补全 HTTP/HTTPS 协议前缀
- **协议自动切换**：HTTPS 访问失败时自动尝试 HTTP
- **全屏视频支持**：支持网页视频全屏播放，包括横竖屏切换
- **沉浸式体验**：自动隐藏状态栏和导航栏，提供无干扰浏览
- **系统代理兼容**：支持使用系统代理设置，适用于公司/校园网络环境
- **本地存储**：自动保存上次访问的 URL，下次启动时自动加载
- **错误处理**：友好的错误提示和重新输入机制
- **返回导航**：支持网页后退功能

## 技术特点

- 基于 Android WebView 组件构建
- 使用 androidx.webkit 库增强 WebView 功能
- 支持 Android 5.1 (API 22) 及以上版本
- 集成 Media3 库支持媒体播放
- 配置了网络安全策略，允许 HTTP 明文流量

## 使用方法

1. 启动应用后，输入要访问的网址（可省略 http:// 或 https:// 前缀）
2. 点击“确定”按钮开始加载网页
3. 浏览网页内容，点击链接可直接在应用内打开
4. 观看视频时，点击全屏按钮可进入全屏模式
5. 按返回键可返回上一页面或退出应用

## 特殊功能说明

### 协议自动补全与切换
应用会自动为输入的 URL 添加协议前缀，默认优先尝试 HTTPS 协议。如果 HTTPS 连接失败，应用会自动尝试 HTTP 协议。

### 全屏视频播放
应用支持网页视频的全屏播放功能，在设备横竖屏切换时能够保持全屏状态，提供连续的观看体验。

### 系统代理支持
应用配置了系统代理支持，能够兼容公司或校园网络环境的代理设置。

### 状态栏优化
应用实现了透明状态栏设计，并根据背景颜色自动调整状态栏文字颜色，确保在各种网页背景下都能有良好的视觉体验。

### 特殊URL Scheme处理
应用现在能够正确处理特殊的URL scheme（如intent://, market://, weixin://, alipays://等），通过系统Intent机制启动相应的应用程序。对于无法处理的scheme，应用会尝试使用系统默认浏览器打开链接。

## 技术信息

- **应用包名**：com.webchecker.mm
- **构建工具**：Android Studio
- **Gradle版本**：8.7

## 构建与开发

### 依赖库

- androidx.appcompat:appcompat:1.6.1
- com.google.android.material:material:1.11.0
- androidx.constraintlayout:constraintlayout:2.1.4
- androidx.webkit:webkit:1.14.0
- androidx.media3:media3-exoplayer:1.4.1
- androidx.media3:media3-exoplayer-dash:1.4.1
- androidx.media3:media3-datasource-okhttp:1.4.1

### 权限要求

- `INTERNET`：访问网络权限
- `ACCESS_NETWORK_STATE`：访问网络状态权限

### 网络安全配置

应用配置了网络安全策略，允许 HTTP 明文流量，支持本地开发和测试环境的访问。

## 注意事项

- 应用在生产环境中关闭了 WebView 调试功能
- 应用会自动保存最后一次访问的 URL，方便下次快速访问
- 支持 Android 5.1 及以上版本


## 版本历史
- **V1.0**：初始版本，包含所有基础功能

## License

本项目采用 MIT 许可证。

## 免责声明

本项目仅作为工具，不存储、上传内容。如有侵权内容，请联系相应的内容提供方。

本项目开发者不对使用本项目产生的任何后果负责。使用本项目时，您必须遵守当地的法律法规。

## 🙏 致谢
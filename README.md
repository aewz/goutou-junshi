# 狗头军师（Android）

![build](https://github.com/aewz/goutou-junshi/actions/workflows/build.yml/badge.svg)

挂在你聊天 App 旁边的对话副驾：读到对方最新消息，先判断、再给可执行的话术，悬浮窗里点一下填入输入框。**发送永远由你自己点，程序绝不自动发。**

- **包名**：`com.aewz.goutou` · **版本**：1.1.0
- **下载**：[goutoujunshi-v1.1.0-release.apk](apk/goutoujunshi-v1.1.0-release.apk)（或见 [Releases](https://github.com/aewz/goutou-junshi/releases)）

## 三种模式

设置 → 军师 → 分析模式，三选一：

| 模式 | 分析面板 | 回复起草 | 依赖 |
|---|---|---|---|
| **经典**（默认） | 判断引擎分析（危险等级、真实意图、建议动作） | 经典人格 3 候选 + 排序 | 判断接口 + 回复接口 |
| **狗头军师** | 军师简析（情绪落地 / 判断 / 建议，≤60 字） | 军师人格 3 候选（第一条为首选） | 仅回复接口 |
| **混合** | 判断引擎分析照跑 | **两组并排**：经典组（排序）+ 军师组，任选一条填入 | 判断或回复接口（判断密钥未配时自动降级为不排序双组） |

## 能力

- **它先判断，再写字**：先把对方意图、危险等级、该不该马上回判断清楚，再据此起草候选回复。
- **不动你的聊天软件**：不 hook、不改包、不读数据库，只用系统无障碍服务读「屏幕上正在显示的对话」。
- **QQ / X 真机跑通**，飞书靠本机 OCR 补正文，WhatsApp 一对一（群聊不读）。未适配 App 可手动「截屏识别一次」。
- **本地知识库**：联系人档案与笔记存本机，分析时自动带上下文；可选记录聊天历史（默认关）。
- **军师知识库随包打包**：58 份参考资料（心理学 / 依恋 / 沟通 / 话术编排 / 法律与危机转介等），设置 → 军师 → 军师知识库，可搜索浏览。
- **接口自己配**：判断 / 回复 / 视觉三路分别可填，流量只发往你配置的模型服务商，作者不运营中转服务器。
- **密钥不落盘**：密钥存在 App 私有空间，从不进日志、不进代码。

## 安装

1. 下载 APK 安装（覆盖安装需先卸载旧签名版本）。
2. 开启**无障碍服务**与**悬浮窗**权限。
3. 设置里填接口（判断 / 回复至少一个），选模式，回聊天页即可。

## 构建

```bash
./gradlew assembleRelease          # 产物 app/build/outputs/apk/release/
GOUTOU_KEYSTORE_PROPS=keystore.properties ./gradlew assembleRelease   # 带签名
```

- Kotlin 传统 View，minSdk 30 / targetSdk 35，Gradle 8.9+（wrapper）或本机 8.14。
- `dl.google.com` 不可达的网络会自动走华为云 / 腾讯镜像（settings.gradle.kts）。
- 签名密钥不进仓库（`*.jks` / `release.properties` 已忽略）。

## 许可与出处

本项目为独立产品，基于两个 MIT 开源项目二次开发，完整出处与限制见 [NOTICE](NOTICE) 与 [LICENSE](LICENSE)：

- [jev-chat/jev-chat-jarvis](https://github.com/jev-chat/jev-chat-jarvis)
- [shengjidaguai-china/goutoujunshi](https://github.com/shengjidaguai-china/goutoujunshi)

[隐私政策](PRIVACY.md)

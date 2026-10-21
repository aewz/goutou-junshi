# 狗头军师（Android）开发说明

## 硬约束（改动前必读）

1. **不 hook、不 Xposed、不改目标 App、不读其数据库**。只用系统无障碍服务与截屏。
2. **绝不自动发送消息**，绝不点任何 App 的发送按钮。填入输入框后停手。
3. **不碰钱**：不触碰转账、红包、收款码相关任何界面元素。
4. **密钥不落盘、不进日志、不进 git**：任何文件里都不许出现 `sk-or-`、`ghp_` 等密钥字符串。
5. 编码一律 UTF-8。
6. 提交与推送由仓库所有者拍板执行。

## 结构

```
app/src/main/java/com/aewz/goutou/
├── capture/   采集层：无障碍读节点（按 App 分发适配器）、截屏 OCR、会话状态、保活
├── core/      模型、Prefs、本地知识库（KbStore / ContextBuilder）
├── engine/    模型客户端：EngineClient（判断+回复门面）、ReplyClient（回复起草，含军师人格）、
│              JudgeClient（判断）、VisionClient（视觉 OCR）、GoutouPrompts（军师人格提示词）
├── overlay/   悬浮窗（气泡/卡片、三模式分组渲染）
└── MainActivity / SettingsActivity / KnowledgeActivity / GoutouKnowledgeActivity
```

## 三模式

`Prefs.advisorMode`：`classic`（经典，默认）/ `goutou`（军师）/ `hybrid`（混合）。
- 经典：判断 + 起草 + 排序，原有管线。
- 军师：军师简析 + 军师人格起草，只走回复接口，不依赖判断接口。
- 混合：判断照跑 + 两组草案并行（无判断密钥时经典组不排序、判断等待立即结束）。
- 密钥门：`hasKeyForMode()`（经典要判断密钥；军师要回复密钥或 OpenRouter 判断密钥；混合任一即可）。

## 已知坑（别重复踩）

- Kotlin 字符串模板 `$x` 后面紧跟中文标点会被解析成标识符 → 一律写成 `${x}`。
- `analysisTasks` 只能在主线程读写；后台任务里要再发任务时回到主线程再 `submitAnalysis`。
- 无障碍 XML 的 `canTakeScreenshot` 改完必须把无障碍关掉再重开才生效。
- `dl.google.com` 在部分网络不可达，settings.gradle.kts 已前置华为云/腾讯镜像，勿删。
- 包名 `com.aewz.goutou`；签名密钥在仓库外（`GOUTOU_KEYSTORE_PROPS` 指定 properties）。

## 构建

```bash
./gradlew assembleRelease testReleaseUnitTest
GOUTOU_KEYSTORE_PROPS=/path/to/release.properties ./gradlew assembleRelease
```

- JDK 17+（本机 21 可用），Gradle 8.9 wrapper（8.14 亦可），compileSdk 35，minSdk 30，arm64-v8a。
- 产物：`app/build/outputs/apk/release/app-release.apk`（已签名、已对齐）。
- 单测：`app/src/test`（会话状态、WhatsApp 规则、军师提示词等，34 项）。

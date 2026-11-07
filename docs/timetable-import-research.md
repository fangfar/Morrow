# 跨学校网页课表导入调研

核查日期：2026-11-07。仅调研，不代表已集成或已通过学校实测。

## 结论

本次没有核实到一个仍有维护、可以直接集成并可靠识别任意学校网页的通用库。可复用的成熟架构是：用户网页登录 → 按教务系统类型选择采集与解析规则 → 转为统一课程数据 → 校验及预览。这里的通用是跨学校复用同一教务系统的规则，不是任意 HTML 都能解析。

建议优先参考 WakeUp 旧开源版本的系统解析器划分，再用小爱课程表式学校脚本补充差异。先核查真实页面和许可证，再选择性移植解析逻辑，不整体接入旧 App。

## 一手源码核查

### WakeUp 旧开源版本存档

查阅的是 `ThaiCao/WakeupSchedule_Kotlin` 存档，不是当前 WakeUp 商业版本源码。README 标注版本 3.612，声明 Apache-2.0；仓库 LICENSE 也为 Apache-2.0。不能从这个旧版本推断当前学校覆盖率或维护状态。

- [README](https://github.com/ThaiCao/WakeupSchedule_Kotlin/blob/master/README.md)
- [LICENSE](https://github.com/ThaiCao/WakeupSchedule_Kotlin/blob/master/LICENSE)

`ImportViewModel.importSchedule` 按导入类型选择新旧正方、URP、新 URP、强智及多种强智变体，还保留学校专用解析器。这比当前 Morrow 的少量表头及文本规则覆盖的结构更多，但仍需要区分系统及版本。

- [解析器调度](https://github.com/ThaiCao/WakeupSchedule_Kotlin/blob/master/app/src/main/java/com/suda/yzune/wakeupschedule/schedule_import/ImportViewModel.kt)
- [解析器目录](https://github.com/ThaiCao/WakeupSchedule_Kotlin/tree/master/app/src/main/java/com/suda/yzune/wakeupschedule/schedule_import/parser)

例如新正方解析器按 `#table1`、`.festival`、`.title` 以及教师、地点、节/周等字段处理；强智解析器有自己的表格标识及文本规则。因此其价值是已有系统规则，而非万能表格推断。

- [新正方](https://github.com/ThaiCao/WakeupSchedule_Kotlin/blob/master/app/src/main/java/com/suda/yzune/wakeupschedule/schedule_import/parser/NewZFParser.kt)
- [强智](https://github.com/ThaiCao/WakeupSchedule_Kotlin/blob/master/app/src/main/java/com/suda/yzune/wakeupschedule/schedule_import/parser/qz/QzParser.kt)

网页采集也按系统不同走不同路径：普通页面读取 DOM；新 URP 会进入课表 callback 页面读取文本数据；部分系统先导航到特定课表入口。这说明只读取可见表格并不足够。

- [网页导入实现](https://github.com/ThaiCao/WakeupSchedule_Kotlin/blob/master/app/src/main/java/com/suda/yzune/wakeupschedule/schedule_import/WebViewLoginFragment.kt)

该旧网页实现还包含忽略证书错误、注入 JavaScript 原生桥等代码；不应整体照搬。README 提到的独立库 `YZune/CourseAdapter` 本次 GitHub API 返回 404，不把它作为可直接依赖的现成库。

### 小爱课程表式 Provider / Parser

核查了 `Arkitect-z/Xiaoai-Schedule-For-New-Zhengfang-System`：README 将流程分为 Provider 采集页面、Parser 输出 JSON；Provider 递归读取 frame/iframe；Parser 对新正方的列表和网格模式分别处理，识别 `#kblist_table`、`#kbgrid_table_0` 等特定结构。仓库 LICENSE 为 Apache-2.0。

- [项目说明](https://github.com/Arkitect-z/Xiaoai-Schedule-For-New-Zhengfang-System/blob/main/README.md)
- [Provider](https://github.com/Arkitect-z/Xiaoai-Schedule-For-New-Zhengfang-System/blob/main/Provider.js)
- [Parser](https://github.com/Arkitect-z/Xiaoai-Schedule-For-New-Zhengfang-System/blob/main/Parser.js)
- [LICENSE](https://github.com/Arkitect-z/Xiaoai-Schedule-For-New-Zhengfang-System/blob/main/LICENSE)

可借鉴采集与解析分离及统一输出格式。这个仓库只是系统适配示例，不代表小爱平台全部能力，不证明脚本仍兼容当前各校，也不能据此直接运行任意网上脚本。

### 较新的组合解析示例

`SaltGardenia/calm-schedule` 的 README 描述了强智、正方旧版、金智 EAMS、课程列表、通用网格的解析链，并列出测试与夹具目录。其 GPL-3.0 许可证及有限的学校验证范围需要单独考虑；本次不将它判定为成熟的跨校通用库。

- [README](https://github.com/SaltGardenia/calm-schedule/blob/main/README.md)
- [导入器目录](https://github.com/SaltGardenia/calm-schedule/tree/main/app/src/main/java/com/saltgarden/kechengbiao/importer)
- [LICENSE](https://github.com/SaltGardenia/calm-schedule/blob/main/LICENSE)

## 对 Morrow 的建议（基于上述源码的判断）

1. 保留已适配学校的直接登录，不用网页通用规则替代已工作的接口。
2. 网页导入使用系统解析器链：识别 URL、DOM、数据字段等特征，选择对应系统和版本的规则；不能识别时允许用户选择教务类型。
3. 按系统分别处理 DOM、内嵌页面和接口数据，不能假定所有课程都在 HTML table 中。
4. 将教师、教室、周次、节次等转换为现有 `Course`；保留周次和节次校验，校验失败不得猜默认值后直接覆盖。
5. 每个引入的规则配匿名合成夹具，覆盖同格多课、单双周、分段周次和课程缺字段；预览核对课程数量、时段与作息。没有样本验证的学校不标为已支持。

优先方向是逐步覆盖常见系统，不是继续给一个正则增加学校名称。现有 `jsoup` 可复用，是否需要脚本运行环境应等采集及解析方案确定后再决定。

## 验证边界

核查了公开源码、README 和许可证文件，未运行第三方项目，未登录第三方学校账号，未验证当前各校覆盖率。本次业务代码保持原状；此前暂停的本地修复仍未提交或安装。

# 开发、测试与发布

## 协作与分支

- 正式开发以公开 `fangfar/Morrow` 的最新 `origin/main` 为基线。新一项正式修改从该基线建立独立 `codex/` 分支；同一工作及其 PR 的后续修复继续使用该分支。合并后开始新工作时重新从最新基线建立分支。
- 新工作开始前检查 `git status --short --branch`、`git fetch origin`、`git branch -vv`、开放 PR 和基线提交；推送前再次核对状态、目标分支和待推送提交。同一工作继续修改时检查相关变化；同步失败如实报告。
- 分叉先比较，无分叉才用 `pull --ff-only`。保留用户未提交的修改，不用强制覆盖、`reset --hard` 或 `clean` 处理差异。PR 合并后检查是否还有独有工作，再按用户授权清理已完成的本地和远端分支；保留仍需使用的分支和工作树。
- 保留现有功能、包名、数据格式及正式签名；复用现有实现，只改请求范围，检查调用方、资源与测试。运行适用检查并审查差异，区分构建、业务回归、仪器测试与真实启动器验收。
- 可完成请求内的本地修改、检查和提交；App 改动推送、创建或更新 PR 需当前用户明确授权。纯开发文档可检查后独立提交、推送并提 PR。合并、正式公开发布、重写历史、删除或归档仓库分别需要明确授权；普通构建不触发这些操作。
- 提交使用已核验属于实际 GitHub 账号的匿名邮箱。交付说明基线、分支、提交、PR 与验证边界；交付 APK 另说明版本、来源、证书及校验值。
- 密钥、凭据、私人课表、设备备份和截图保存在仓库外；公开迁移只提交已审查文件，不合并或镜像推送私有历史。设备测试按“设备验证”备份数据、核对证书并使用隔离 QA 包与合成数据。
- README 和 App 内的软件说明面向用户，只介绍功能、下载、使用、隐私、反馈及必要许可信息；开发流程放在本文，版本更新说明放在 Releases，不新增重复状态台账。

## 正式与预研隔离

- 任务页及学习通基础作业功能纳入正式开发范围：账号验证、本机加密配置、主动刷新、课程名称与作业展示、按账号离线保存。合并源码不等于发布 APK；正式签名与公开发布仍需单独授权。后续未开放的平台和社交继续隔离开发。
- 日常维护使用当前正式仓库，以最新 `origin/main` 为基线。`codex/development-isolation` 仅保存本隔离约定。
- 已开放的任务页、底部导航和学习通基础功能在正式线维护；未开放的平台、新界面与未来社交存放在独立 Git worktree，分支为 `codex/task-page-navigation`。使用 `git worktree list` 查找目录，不复制另一套仓库；只把正式修复同步进预研分支，不把预研分支合入日常分支。
- 用户未明确指定新功能时，“修复、推送、打包 App”均针对正式开发线；预研分支只在用户明确要求时推送。开放某项功能需单独授权、整理差异、验收并通过 PR 合入，合并及公开发布沿用既有授权边界。
- 每次打包前核对工作目录、`git status --short --branch`、来源提交和相对 `origin/main` 的差异；从已提交源码构建，交付注明正式或预览、分支和提交。正式构建不得直接使用预研 worktree，既有签名发布工作流仍只允许 `main`。

## AI 助手

- AI 与课表底栏切换保留聊天草稿、滚动及正在生成的回复；考试按真实时间放在对应日期列，与重叠课程分列；连续考试直接跨原有节次，课间不插入额外行或时间列表；课表范围外的考试保留边界展示空间，实际时间写在卡片中，不单独增加考试时间轴。考试名称与详情按课程字号连续排版，名称可充分换行。未知开始时间仅在日期标记及详情显示，未知结束时间明确标注；可选 end_time 保留旧考试数据兼容。切周同步调整网格高度，课表缓存检查考试和作息变化。缓存课表重新挂载时须让 WeekPager 完成布局，避免首次横滑才重置位置。回归包含 `TimetableExamTest` 与隔离包 `aiOnly` 仪器检查；`TimetableReturnChecks` 覆盖 AI 返回后的首次左右滑动。

- 聊天主界面顶部提供统一侧栏、独立临时聊天和菜单；强度通过输入框内轮盘选择。AI 主页面返回直接退出，课表右上角菜单不再重复提供 AI 入口；发送和停止共用居中的 ImageButton，强调色统一使用 PRIMARY；思考强度使用保持键盘的分档滑杆浮层，粉色填充跟随滑块，切换沿用 Motion 动效；加号菜单不夺取输入焦点或收起键盘。侧栏与底栏共用四项导航的描边 / 实心图标，未选中弱化，选中使用前景色；侧栏合并模块导航与项目聊天列表，底部左侧新聊天、右侧头像设置；项目菜单进入独立项目列表与新建页。临时聊天仅存内存，不读取已有项目记忆，不写入聊天历史；切回普通聊天或进程结束后清除。已移除应用内语音识别、麦克风按钮、设置入口及识别活动声明；不申请 RECORD_AUDIO。附件加号提供拍照、照片、文件与强度入口；手机信息与相机权限统一在设置 → 权限管理。菜单使用共享 MotionOverlay，左下方覆盖加号，去除空白底栏和 × 控件，原加号位置的重复触摸由覆盖面板处理；重复点击加号或收起键盘关闭；强度档位同步更新轮盘指针。图片直接上传 DeepSeek Files API，文件 ID 随多模态消息发送，上传在按发送时发生，失败可重试，远端文件 30 天自动过期。图片不再需要 OCR 插件；单图限 8 MB / 3200 万像素，文件读取限 10 MB；UTF-8 文本和 DOCX 提取正文（最多 64000 字），PDF 由系统渲染为图片（最多 4 页），每次最多 4 个手选附件或页面，独立于 2000 字输入框。聊天长按确认后可删除，保留项目记忆；最后一个聊天删除后补空聊天。所有主页面与二级页使用共享顶部边距，主页面大字号标题放到按钮下方；菜单不改变输入框行数。主题默认粉色，用户可在外观选择强调色，删除动作保留警示色。用户消息使用主题浅粉气泡，助手正文无背景框；标题按级别区分字号和粗细，正文、加粗与等宽代码分别呈现，流式与历史消息使用同一排版。对话滚动层铺满聊天页面，允许文字滚入顶部按钮和输入框后方；上下渐变保留透明度，不以实色区域截断正文，首尾仍留有完整阅读的滚动空间。考试详情与考试记录提供手动删除，确认时校验当前课表和完整记录快照，保存成功后刷新课表及小组件。具体周次按钮保持无阴影；任务分类采用字段内的下拉箭头，课程选择复用强调色菜单相同的锚点浮层，保持草稿和输入焦点、右侧标记当前分类。未显示键盘时输入框收成单行胶囊，显示键盘后展开工具区；切换保留草稿与选区，菜单不改变展开状态。流式回复期间发送按钮切换为停止，取消后的工具建议不能执行；用户向上阅读时不强制滚到末尾。

- 普通对话使用流式文本，应用变更通过工具调用单独解析和确认；DeepSeek 强度分别对应关闭思考与 low/high/max，兼容协议仅在配置声明支持时传递 none/low/medium/high。聊天、项目和项目记忆保存在私有 `ai_chats` 偏好中，跨重启保留；每次请求使用最近32条消息及当前项目记忆，并由设备时钟即时提供日期、时间、星期、时区与偏移量。手机上下文另含机型、Android 版本、电量；日历与通知分别启用并取得系统授权后才在发送时读取，日历范围为昨天起未来 30 天 / 最多 100 条，通知仅当前最多 20 条且屏蔽验证码正文。独立 AI 通知监听器不复用或改变快递监听行为。MediaProjection 屏幕共享每次由系统授权，使用前台服务、持续通知和停止入口；只在内存保存最近外部画面，返回 Morrow 时暂停更新，发送问题时上传该快照并附捕获时间，停止共享清空帧，不自动重启或持久化投屏令牌。项目记忆可编辑、清空，模型可按用户授权自主追加、去重、替换和清空项目记忆，显示保存状态并提供一次撤销；写入前比较项目及记忆快照，临时聊天不保存长期记忆，凭据和验证码不得自动记忆。网络响应与工具参数仍在本机校验，不直接执行模型返回的文本。

- 底部 AI 与课表、任务、快递并列；跨模块请求提供当前本机任务和快递快照。任务操作沿用 TaskPage 的串行保存、账号隔离与恢复历史；快递沿用 ParcelRepository 的校验、去重、恢复、通知和组件更新。目标变化时拒绝写入。平台任务只变更本机记录，不代表向学习通提交作业。缺少手机读取授权时直接请求系统日历、通知或屏幕共享权限，返回后继续原问题，不跳转 Morrow 权限页。手机操作经确认后使用原生 Intent 打开已安装应用、设置、拨号器、闹钟、计时器，或调整媒体音量；不直接拨出电话，闹钟与计时器保留系统确认界面，不支持任意第三方界面点击，不让模型接触密码或密钥。
- API 配置支持最多 20 份供应商配置，包含可编辑的名称、HTTPS 基础地址、模型、协议、推理能力与密码遮罩下的原密钥，可新增、选择、编辑及确认删除。配置和密钥整体使用 Android Keystore + AES-GCM 加密并原子保存；首次从原 DeepSeek 密钥构造默认配置，持久化后保留其他配置，删除旧 DeepSeek 配置同时移除旧密钥。请求开始时固定所选配置，禁止跟随 HTTP 重定向，不向其他供应商转发已有远端文件 ID。DeepSeek 附件保留 Files API；兼容接口采用压缩图片 data URL，受现有聊天容量上限约束。参照 [DeepSeek Chat Completions](https://api-docs.deepseek.com/api/create-chat-completion/) 和 [OpenAI Chat Completions](https://developers.openai.com/api/reference/resources/chat)。
- 共享独立主操作占满内容宽度，成组的保存、删除、取消、恢复等操作使用 `SecondaryUi.actionRow` 横排并保留 48dp 触控高度；API 编辑、项目记忆、快递读取与提醒复用该布局。聊天侧栏进入设置须记录 AI 来源，返回时复用原聊天及草稿；不保留应用内语音识别入口。
- 全局操作检查包括 `AiModulePlanTest`、`AiModulesIntegrationTest`、底部/侧栏导航测试；Windows 文件替换测试复用 `DataManagementTest.AtomicRename`。

- 目前接入 DeepSeek Chat Completions，以用户自行提供的 API 密钥认证；密钥用 Android Keystore 加密保存在 `deepseek_key` 中，可在助手设置中删除，不进入备份、日志、课表或对话。发送时将当前课表、有效单次调整、当前课表的手动考试记录、任务与快递数据及最近对话发给 DeepSeek；界面需明确告知和提示费用。
- 考试记录保存在对应课表偏好的 `aiExams`，日期必填，时间可未知；数据管理可单独清空。它不代表学校考试查询，也不自动同步。模型只能提出一个操作，所有添加、修改和删除都先展示并确认；应用校验字段、节次与日期，确认前检查当前课表和快照是否变化，保存失败时尝试恢复原数据。修改或删除课程可能使单次调整失效，须提示。
- 执行 `DeepSeekPlanTest`、`DeepSeekAssistantTest` 和相关界面测试；真实 DeepSeek 连接、密钥加密与设备键盘交互需另行验收。模型回复可能出错，不能把自然语言回答或确认前的建议当作已执行结果。

- 设置移除重复的 AI 助手跳转，直接提供 API 配置、项目记忆及简要数据说明。拍照使用 CameraX 1.4.2（兼容当前 compileSdk 34）的应用内底部取景面板，按需请求相机权限；取景不显示操作提示，PreviewView 提供双指缩放和轻点对焦，支持前后镜头切换。离开应用或关闭面板释放相机；照片只存私有缓存，读取后清理。附件显示缩略图，点开可在应用内缩放、拖动与双击还原。真实相机需手机验收。参见 [CameraX 架构](https://developer.android.com/media/camera/camerax/architecture)。

- 外观提供十种强调色（默认粉色），选择保存在独立偏好键 `accentColor`。共享 `Appearance` / `SecondaryUi` 与主题属性统一控制按钮、焦点边框、原生控件、选中面和聊天气泡；黑色在深色模式下使用浅色前景，明暗模式分别配置对比。切换重建后保留外观页和普通聊天草稿、选区。
- API 添加页提供 DeepSeek、通义千问、智谱 GLM、Kimi、豆包及硅基流动预设，并提供空白自定义配置；预设只填表，不自动保存、选择、发请求或改写已有密钥。豆包需填账号已开通的模型 ID，其余预填模型也可编辑。地址参照 [百炼](https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope)、[智谱](https://docs.bigmodel.cn/cn/guide/develop/openai/introduction)、[Kimi](https://platform.kimi.com/docs/api/overview)、[火山方舟](https://console.volcengine.com/ark/region:cn-beijing/docs/text-generation)、[硅基流动](https://docs.siliconflow.cn/docs/api/chat-completions-post)。兼容接口默认关闭推理强度参数，避免将供应商特有字段混用。各平台实际账号调用需单独验证。

## 任务与学习通

- 学校账号、学习通账号与 API 配置复用二级页、表单字段及底部固定操作区；学习通账号不再使用登录对话框，离开时清空密码并解除该页防截屏标记，验证中禁止返回，验证失败保留原配置。
- 账号验证成功后才使用 Android Keystore 加密保存凭据；密码不回填、不记录日志或导出。学习通接口只信任指定域名和标准端口，禁止跨域转发凭据；错误信息不暴露响应、地址、Cookie 或签名。
- 先读取本机缓存，刷新失败保留任务；网络响应须与最新本地数据合并，不能覆盖用户编辑、删除、恢复或设置。手动任务独立保存，切换账号不影响；课程关闭时隐藏其任务，重新开启可恢复。
- 缓存沿用 schema 1 兼容旧数据，并使用 AtomicFile 保存。按课程 / 作业标识合并；失败、分页未读和无法唯一对应的数据不删除，用户明确编辑或清除的截止时间不被覆盖。
- 按课程分组展示未完成任务，删除支持撤销和恢复，不改变平台提交状态。无年份的有效截止日期按当前年份计算倒计时，不修改原始数据；未知截止时间不显示时间行，开始时间不能充当截止时间。
- 修改任务功能时检查 `TaskDeadlineTest`、`TaskPageDesignTest`、`ChaoxingDeadlineTest` 和相关缓存、刷新测试；第三方平台适配还需实际账号验证，本地测试不代表平台端到端通过。

## 开发与构建

### 界面与目录约定

- 面向用户的名称统一为 **Morrow**，官网为 `https://morrow.xvyan.cn/`，仓库为 `fangfar/Morrow`；`cn.courselens` 包名、偏好键、签名、发布资产和工具参数保留兼容名称。
- `app/` 维护原生 Java/View 界面；`web/` 为官网源码，`remote/` 为发布配置；业务检查在 `tests/`，Android 单元与仪器检查分别在 `app/src/test/`、`app/src/androidTest/`。生成目录与验收截图不提交。
- README 介绍功能、安装、隐私与反馈；官网展示实际产品及下载；开发约定集中在本文，更新记录使用 Releases。历史发布说明按对应版本保留，不将新源码能力写成已发布能力。
- 系统无衬线字体，主页面标题 24sp、二级页标题 18sp、正文 14–16sp、元信息 12–13sp；用字重、对齐和间距分层，课程分组不再套装饰性胶囊。课表周网格、课程配色与底栏直接切换保留。
- Android 色板以 `values/colors.xml` 与 `values-night/colors.xml` 为准，快递色值通过资源别名复用；官网同步墨色 `#171717`、次要文字 `#626870`、浅底 `#F3F4F5`、动作蓝 `#0866D9`。仅状态和操作使用强调色，课程颜色继续承担区分课程的功能。
- 复用 `SecondaryUi` 的尺寸常量、按钮、焦点和弹窗：间距按 4dp / 8dp 网格，普通操作至少 48dp，主要表单输入与课程选择控件至少 56dp；普通卡片使用 16dp 内边距与圆角，大面板使用 24dp 圆角。方形组件保持 1:1，横向区域由父容器宽度及内容决定尺寸；桌面组件按系统分配尺寸适配。Web 点击范围至少 44px，内容变窄时换行或纵向排列。动效沿用 `Motion`，遵循系统关闭动画及网页减少动画设置。
- 空状态提供清楚的状态和可执行操作；删除重复标题、标语与操作说明，不删除错误恢复、隐私、授权、数据覆盖提示和无障碍名称。设置按课表与应用分组，模块配置继续由各模块菜单直接进入。
- UI 修改同时验证原调用方、返回来源、草稿、滚动与状态恢复。可用 `UiConsistencyTest`、`TaskPageDesignTest`、`ParcelPageDesignTest` 和 `ModuleSettingsStyleTest` 覆盖布局及本机交互；原生 Canvas 渲染不能代替设备手势、键盘、系统安装器或真实启动器验收。截图使用合成数据，输出至仓库外；官网三张截图由 `UiConsistencyTest.productScreensUseSyntheticDataAndKeepActions` 的 360dp / xhdpi 场景生成，不使用私人课表。

### 交互与动效

- 保留现有导航、返回来源、滚动位置、编辑草稿和无障碍焦点；删除数据分别确认，失败保留原数据。弹窗、按钮和动效复用 `SecondaryUi`、`Motion` 及现有面板组件，遵循系统动画设置。
- 课程详情保留半屏；选择面板挂载在外层 `windowContent`，系统栏和键盘边距只处理一次。拖动、预测性返回和嵌套面板须支持取消，已改草稿退出前确认。
- 界面变更运行相关 `UiConsistencyTest`、`TaskPageDesignTest`、`PopupGeometryTest` 或 `MotionSystemTest`；JVM 检查不代替设备手势、键盘、系统安装器或真实启动器验收。

安装 JDK 17、Python 3、Android SDK 34 与 build-tools 34.0.0。配置 `ANDROID_HOME`，或在被忽略的 `local.properties` 指定 `sdk.dir`。

```powershell
python tests/run_checks.py
python tests/ReleaseChecksTest.py
python tests/SigningChecks.py
.\gradlew.bat --no-daemon -PqaBuild assembleDebug :app:assembleLiteDebugAndroidTest :app:lintLiteDebug
```

Linux 使用 `python3` 和 `sh gradlew`。`run_checks.py` 是本地与 CI 共用的 Java 业务回归入口，另含本实验的学习通解析检查；Gradle 单元测试的 `NO-SOURCE` 不代表它们通过。

普通贡献者使用 `-PqaBuild`：Debug 包名为 `cn.courselens.qa`，使用开发签名；测试数据在测试 APK 中，Release 变体禁用。正式构建保持 `cn.courselens`，必须提供既有签名密钥并通过固定证书检查。

## 快递中心

### 单一主应用

- 后续只构建和发布一个主应用 APK，不提供快递图片识别或 OCR 插件。内部保留 `lite` 构建名以兼容脚本路径，不代表对用户提供多个版本；公开文件统一命名 `Morrow-版本号.apk`。
- 快递支持通知、粘贴文字、文字分享和手动录入；保留已有记录、撤销与恢复、提醒和桌面组件。删除图片分享入口、识图桥接与下载页面、OCR 引擎和插件 Gradle 模块；不再申请安装其他应用的权限。
- AI 聊天拍照和照片附件继续由现有多模态链路处理，不依赖 OCR。为保持已授权 URI 兼容，FileProvider 保留原 authority，仅开放私有 `ai-capture` 缓存路径。
- 既有版本和远程配置的历史字段保留兼容，不删除已发布资源；当前构建和发布流程不再生成、打包或发布插件。

### 快递测试

```powershell
python tests/run_checks.py
.\gradlew.bat --no-daemon -PqaBuild assembleDebug :app:assembleLiteDebugAndroidTest :app:testLiteDebugUnitTest :app:lintLiteDebug
adb -s SERIAL shell am instrument -w -e parcelOnly true cn.courselens.qa.test/cn.courselens.NativeChecks
```

`ParcelRulesTest` 使用合成文字覆盖取件码别名、多码、身份码和电话 / 日期排除；`ParcelAndroidTest` 验证数据库和通知识别的去重、撤销、恢复、清空与授权隔离。仪器专项 `parcelOnly` 只用 QA 合成数据；真实通知和桌面小组件仍需设备验收。

每个可用 Android 13 / 14 / 15 / 16 及以上的 QA 设备还需检查：首次拒绝/允许通知、频道关闭、通知访问授权/撤销与重新绑定、后台识别关闭后无写入；不同来源/重复通知/敏感通知过滤；文字分享、多码确认；深色及 2 倍字体；Doze / 重启 / 强行停止后的提醒延迟和提醒开关；真实启动器固定请求、尺寸、取件后刷新和隐藏码值。无设备时只能交付构建及模拟结果，不能宣称这些端到端检查通过。64 位原生库的 16 KiB ELF / APK 对齐检查也不能代替 Android 15+ 16 KiB 页设备运行。

## 多份课表与学校适配

- 当前课表 ID 和名称列表放在 `timetables` SharedPreferences；原有课表继续使用 Activity 原偏好文件，不复制或改写已有课程格式。设置中的当前课表与切换列表支持长按名称改名，名称校验沿用 1–40 个字；只更新索引，不搬移课程数据。从设置进入新增页，取消或手动创建后返回设置；首页入口返回课表页。新增课表使用独立偏好文件，课程、学期、备注、冲突选择、调课与 `classTimes` 随课表切换。小组件读取同一当前 ID，旧课表的小组件点击不会误匹配新课表。学习通账号和作业仍使用原有独立存储。
- `ClassTimes` 以每行 `HH:mm-HH:mm` 保存作息，1–30 节，检查顺序、重叠与课程节次上界。作息页按节次显示上课 / 下课时间，使用 24 小时选择器，支持新增一节与移除末节，并沿用保存 / 放弃草稿提示。未保存该字段的旧课表沿用宿迁学院 12 节作息。新增可手动创建或选择学校导入；徐州工程学院通过个人课表接口读取已选课程及各校区日节次，支持分段周次、单双周、单周调课与无时间实践备注。不同校区作息不一致时明确拒绝合并，保留旧课表。
- 徐工院仅放行 `jwglxt.xzit.edu.cn` 的标准 HTTP/HTTPS 端口，禁止跨域或 URL 用户信息，密码使用学校 RSA/PKCS1 协议；登录成功后按课表使用 Android Keystore 加密保存账号密码，供静默刷新，不保存 Cookie。学校自动登录客户端仍限制精确域名；为兼容其他学校的网页导入，Android 网络配置允许 HTTP，其他模块继续执行各自的地址校验。该校当前 HTTP 服务不提供传输完整性和会话保密性，登录页及 README 明示限制；HTTPS 可用后应移除此例外。
- 使用 `-e timetablesOnly true` 运行多课表与徐工院合成专项，涵盖旧数据保留、新建与切换、重建恢复、14 节编辑与小组件、RSA/解析及域名边界；测试只使用临时合成课表，结束恢复索引。真实账号读取只验证第三方链路，不把私人课表或凭据放入测试资产。
- 学校课表同步位于课表下拉菜单的「同步课表」，课表管理页不显示刷新入口；`schoolImported` 随各课表保存，旧数据通过学校同步备注识别。每个课表绑定各自学校账号：登录成功后使用 Android Keystore 加密保存，刷新先复用当前会话，会话过期或进程重启后读取凭据静默登录；验证码、凭据失效或学校不匹配时进入登录页，账号可预填、密码不回填。清空当前课表时删除该课表保存的账号。仅刷新成功后覆盖课表；失败、无效数据或页面已切换时保留原课表。`timetablesOnly` 同时覆盖刷新入口、后台读取、成功保存、失败保留、静默登录、验证码回退和凭据存取。

- 其他学校通过「网页导入」自行登录 HTTP / HTTPS 教务网页，读取当前页及同源框架，按系统特征调用 `WebCourseParser`：新正方网格 / 列表及 kbList JSON、旧正方 Table1、强智 kbcontent、URP 合并明细表和新 URP 周掩码 JSON，通用结构分析不依赖学校 ID：根据星期标题与节次行重建普通 / 转置周网格，展开合并单元格；明细表支持表头别名、多级表头、合并行及复合上课时间列；表格外有明确课程名标记的卡片也可解析。最后保留原有通用表格作为补充。支持明确字段、合并单元格、同格多课和非连续节次，不猜测缺失周次或节次；未标明教师或教室的文字保留为原文补充并在预览展示。同格课程无法分开、星期 / 节次与位置冲突、存在未解析的周次片段时整次导入失败，不以部分结果覆盖。网页采集脚本仅对已识别的新正方 / 新 URP 页面调用对应的只读课表接口，使用同源会话，拒绝重定向并限制大小和等待时间；返回接口数据优先于同页 DOM。宿迁学院 VPN 个人课表页复用 `SchoolClient` 的 get-data / print-data 接口及 JSON 解析，仅读取对应域名的当前网页 Cookie，在内存使用后释放，不保存网页密码；其他网页按系统解析器及通用表格规则处理，识别到不支持的系统变体时停止导入。结果必须预览并核对作息后保存；空结果、解析失败或返回时课表已改变均不覆盖数据。不支持图片、Canvas、PDF 及跨域框架。网页顶部使用紧凑操作栏与网址行，正文占满剩余空间和可用宽度；开启宽视口、整页概览与双指缩放以兼容电脑端教务页面。网页提供「上一页」与「关闭网页」操作，允许 HTTP 网页及其资源，界面提示 HTTP 连接不加密。网页不注入原生桥、不读取密码、不放行证书错误、文件或外部应用跳转；WebView 保留 Cookie 与网站存储，进入 / 退出网页导入不自动清理，离开时刷新持久 Cookie；登录是否过期由学校会话策略决定。仅按课表保存站点根地址，不保存含令牌的完整 URL。总设置 → 数据管理 → 清理网页数据，经确认后清除所有网页导入站点的 Cookie、网站存储、HTTP 认证和缓存，不删除课表或单独保存的学校 / 学习通账号；已适配学校继续复用各课表的加密账号，学习通继续复用其独立加密凭据。

- 网页导入回归：`WebCourseParserTest`、`GenericWebCourseTest`、`SchoolWebSessionTest`、`WebTimetableTest`、`WebTimetableLayoutTest`；采集脚本运行 `node tests/WebTimetableExtractTest.cjs`。格式参考 WakeUp 3.612 开源解析器，Apache-2.0 许可及来源保存在 `assets/licenses/wakeup-parsers.txt`。合成用例通过不等于所有学校实测通过。

## 设备验证

先核对 `adb devices -l` 的目标序列号，备份 QA 包已有偏好。只覆盖安装 QA 主包和测试包，不卸载用户正式包。

```powershell
adb -s SERIAL install -r app/build/outputs/apk/lite/debug/app-lite-debug.apk
adb -s SERIAL install -r app/build/outputs/apk/androidTest/lite/debug/app-lite-debug-androidTest.apk
adb -s SERIAL shell am instrument -w -e saveOnly true cn.courselens.qa.test/cn.courselens.NativeChecks
```

`SERIAL` 替换为已核验序列号，手机保持解锁和 App 前台。参数可换为 `manualExamOnly`（考试详情手动删除、取消、记录与课程保留）、`chatMotionOnly`（合成聊天草稿、真实键盘升降、输入框连续高度、附件菜单与设置权限入口）、`tabStartupOnly`（冷进程下 54 份合成缓存作业的首次 / 后续底栏点击、首帧及主线程停顿）、`capsuleOnly`（冷启动恢复非零筛选、加载布局变化后的首次滑动、反向取消、点击动画及底栏无水波纹）、`chaoxingOnly`（学习通只读客户端、课程开关、删除恢复与刷新保留的合成检查）、`navigationOnly`（任务页导航、紧凑操作及普通 / 大字体布局）、`choiceOnly`（课程与调课选择器、取消和草稿保留）、`dialogLayoutOnly`（普通与大字体弹窗正文、按钮分区）、`courseSlotsOnly`、`scrollOnly`、`conflictOnly`、`adjustmentOnly`、`widgetOnly`、`widgetGuideOnly`、`widgetPinOnly`、`motionOnly`、`gestureOnly`、`performanceOnly` 或 `remoteOnly`；不指定专项参数运行全量入口。`nextClassOnly` 是历史名称，首页已无下一节提示。

`navigationOnly`、`choiceOnly`、`dialogLayoutOnly` 可追加 `-e normalFontOnly true`，只检查设备当前字号，不临时放大字体；未指定时仍覆盖普通与大字体。

测试使用合成数据；正式 Activity 与 QA Activity 的偏好文件名不同，按实际包与文件核实。测试结束或中断后恢复并比较备份，不能仅凭测试的 PASS 推断用户数据未变化。小组件添加、尺寸和点击另在真实启动器验收，测试宿主不能替代它。

`widgetOnly` 和 `widgetGuideOnly` 需要 QA 测试宿主的临时绑定权限；只对 QA 包授予，在 `finally` 中撤销。它不是厂商桌面快捷方式权限，也不证明真实启动器添加成功：

```powershell
adb -s SERIAL shell appwidget grantbind --package cn.courselens.qa --user 0
try {
  adb -s SERIAL shell am instrument -w -e widgetGuideOnly true cn.courselens.qa.test/cn.courselens.NativeChecks
} finally {
  adb -s SERIAL shell appwidget revokebind --package cn.courselens.qa --user 0
}
```

真实启动器验证使用 `widgetPinOnly`，先撤销测试宿主绑定权限；它确认实际新增绑定和系统成功回调，结束后保留 QA 组件供查看。厂商拒绝请求时记录失败及手动添加指引，不把指引当作成功，不更改厂商权限绕过。

正式发布前核验：已有课表启动恢复、同步失败保留数据、课程保存与取消、多时段、调课/停课、冲突选择、切周及滚动返回、小组件、公告和更新。覆盖更新在隔离设备上保留真实格式的合成数据验证，不卸载旧版本。

### 复用隔离发布模拟器

在 Windows 上复用专用于发布验证的 AVD：

```powershell
.\tests\start_release_emulator.ps1 -AvdHome "<隔离 AVD 目录>"
```

脚本只启动或复用名为 `morrow-release-test` 的模拟器，不清空数据、不安装 APK；返回的 `emulator-5556` 是后续 adb 目标，不能替换为连接的真实手机。可以通过 `-AvdHome`、`-AvdName`、`-Port` 指定已建立的隔离 AVD；缺失 SDK 或 AVD 时先安装并创建，已有模拟器镜像及系统授权沿用，不重复下载。每次验证前确认该 AVD 只含合成数据，旧正式包、测试数据和新版签名 APK 分别核对来源与证书；按实际升级路径覆盖安装并验证课表、任务、快递与数据库迁移。脚本启动成功不是覆盖升级或真人体验验收。

## GitHub Actions

- `Android checks`：必需检查始终名为 `checks`。PR 仅修改根目录或 `docs/` 下的 Markdown 时运行文档差异检查；仅修改 `remote/config.json` 时运行配置和网站验证。main push、空差异、无法判断、其他文件或混合改动均执行完整 Android 检查。完整检查缓存 Gradle 依赖，验证无密钥签名边界、QA 主/测试 APK、单元测试和 Lint；不执行设备测试。
- `Remote configuration`：PR 只验证配置，main 上相关文件变化后部署现有 Pages；不构建或发布 APK。
- `Prepare signed release`：仅手动选择 main，拒绝已存在标签及不递增的 versionCode，构建与验证正式 APK，生成校验文件和来源元数据，创建**草稿** Release。

合入迁移 PR 后，为 main 启用 PR 与必需检查 `checks`，禁止强推；个人维护不要求第二位审批人。配置 `release` Environment 的部署分支仅允许 main，然后才放入以下 Environment secrets：

```text
COURSELENS_KEYSTORE_BASE64
COURSELENS_STORE_PASSWORD
COURSELENS_KEY_ALIAS
COURSELENS_KEY_PASSWORD
```

使用既有密钥，不生成新密钥；先完成仓库外的安全备份。密钥不提供给普通检查。工作流生成后不会自动迁移私有仓库 secrets，也不执行任何发布。

## 正式发布

1. 合入已批准并完成适用设备验收的源码；提高 versionCode 和 versionName。保留包名、偏好名称、存储格式及签名指纹；修改存储时另做旧数据兼容检查。提前并行核对版本与标签、准备隔离模拟器及合成数据；本地独立检查与远端 CI 可并行，不重复运行已通过且源码未改变的检查。
2. 经发布授权后，main 必需检查通过即运行 `Prepare signed release`，选择 main 和与 versionName 对应的新标签；在授权范围内处理 release 环境审核。签名构建和独立的隔离覆盖升级准备可并行，检查结果中的来源提交必须是获准提交。
3. 核对草稿 APK、`SHA256SUMS`、`release-metadata-lite.json`、证书及使用说明，完成隔离覆盖升级验证；经明确授权后公开草稿。现有标签和 APK 不替换。
4. 发布成功且 APK 可下载后，再通过 PR 更新 `remote/config.json` 中的版本、更新日志和 APK 地址，保留公告及人工维护的网盘地址。CI 成功或草稿 Release 不等于发布成功。
5. 核验 Pages 与 Raw 均返回 200，配置与 Git blob 一致；检查 APK 下载及旧客户端更新入口。网盘分享由维护者核对内容和有效期。

本地正式构建按 `signing.properties.example` 配置被忽略的签名属性，密钥保存在仓库外；执行 `python tests/SigningChecks.py --signed`、正式 `assembleRelease :app:lintLiteRelease`。`python tests/ReleaseChecks.py vVERSION --apk PATH`使用 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT` 检查产物，输出到 `app/build/release/`，不发布。

## 公告配置与旧版兼容

网站介绍页在 `web/`，使用静态 HTML/CSS；`python tests/WebsiteChecks.py` 检查下载地址、站内锚点、图片属性及本地资源。预览时先运行 `python web/build.py`，再以 `preview/site/` 为站点根目录启动静态服务器。`Remote configuration` 在相关文件变化时检查页面与配置，main 部署时运行 `python web/build.py site`，将网站和 `remote/config.json` 打包到同一 Pages 站点，配置路径仍为 `/remote/config.json`。三个下载入口由 `web/download.js` 在点击时读取同站点的 `remote/config.json`，校验 `android.apkUrl` 后从 GitHub 直接下载 APK，不调用 GitHub 查询 API。最新版以更新配置为准；正式 APK 发布后按发布流程更新配置即可。配置请求超时、失败或地址无效时使用构建时的同版本地址下载；构建时从同一配置生成按钮的直接 APK 地址，禁用 JavaScript 时仍可下载对应版本。`node tests/WebsiteDownloadChecks.cjs` 检查下载选择、失败处理及重复点击。图片仅使用经过检查的合成示例，原始手机截图与设计验收截图保存在仓库外。

当前源码仅请求 `https://morrow.xvyan.cn/remote/config.json`，不再尝试 GitHub Pages 或 Raw 备用接口。连接、HTTP 或配置校验失败时保留有效缓存，并沿用检查失败提示；请求仍不跟随 HTTP 重定向。

仓库入口及配置中的 `repositoryUrl` 统一为 `https://github.com/fangfar/Morrow`，APK 下载链接使用新仓库路径，已发布资产的文件名保持实际名称。保持 `schemaVersion: 1` 和 `remote/config.json`。新版 App 的精确仓库校验与新配置必须配套部署；旧缓存中的仓库地址不再接受。已发布旧客户端的接口和仓库校验固化在 APK 内，无法由此次源码修改替换；旧版升级路径需在发布前单独核验。

公告 ID 唯一；修改同一 ID 不会重新提醒，需要新公告时用新 ID。配置最多 64 KiB / 20 条公告，链接为 HTTPS，版本以实际 versionCode 判断；检查失败保留有效缓存，不冒充“已是最新版”。线上配置校验与 App 的 `remoteOnly` 专项分别验证。

新打开 Activity 时在首帧后检查更新，从后台重新打开也会检查，绕过常规六小时缓存；Activity 重建沿用进程内请求和提醒状态。“设置 → 关于软件 → 软件更新”二级页面提供自动检查开关、手动检查及更新状态，关于页只显示更新入口，并提供官网与 GitHub 仓库入口，不显示网盘下载。自动检查开关默认开启，保存在独立 `appSettings` 偏好中，不随课表切换。关闭后不自动提示普通更新，但启动仍读取配置以判断重大更新策略。更新弹窗的“取消”只抑制本次打开；公告已读状态保持原行为。`remoteOnly` 覆盖请求、开关持久化、更新子页、强制提示与本地开关、过期策略、已安装版本、远程文案和取消去重。

更新配置沿用 `remote/config.json` 的 `android` 对象及 `schemaVersion: 1`，新增字段均可省略，旧客户端会忽略它们：

```json
{
  "forcePrompt": false,
  "promptTitle": "",
  "promptMessage": ""
}
```

`forcePrompt: true` 会在存在较新版本且配置未过期时，绕过本地自动检查开关显示更新弹窗，每次打开最多一次；它不强制安装，也不锁住 App。“取消 / 立即更新”继续可用。`promptTitle`（最多 160 个 UTF-16 单元）、`promptMessage`（最多 12000 个 UTF-16 单元）分别配置弹窗标题和正文，空文本回退到版本标题和 `changelog`。布尔值和文本类型严格校验，无效配置保留有效缓存。发布重大更新时先按发布流程发布 APK，再更新版本、下载地址、文案及 `forcePrompt`；不提高版本号时，已安装该版本的客户端不会提示。当前配置默认关闭强制提示，不代表已发布新的 APK。

## 原私有仓库归档条件

从公开 main 干净检出通过构建与业务回归、关键设备验收、签名和覆盖更新验证；确认必要文件、受控发布 secrets 及密钥备份齐全后，才切换本地远程。保留私有 Git 历史的安全备份，经单独授权后保持私有并归档，不删除。

公开历史和 v2.6 APK 曾包含真实样例课表。新源码改为合成测试数据不会清除旧记录；历史清理及旧发布资产处理需单独评估和授权，不以镜像推送、强推或重新公开私有历史解决。

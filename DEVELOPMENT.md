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

- AI 与课表底栏切换保留聊天草稿、滚动及正在生成的回复；考试按真实时间放在对应日期列，与重叠课程分列；课间及课外考试扩展时间轴，不单独占考试行。未知开始时间仅在日期标记及详情显示，未知结束时间明确标注；可选 end_time 保留旧考试数据兼容。切周同步调整网格高度，课表缓存检查考试和作息变化。缓存课表重新挂载时须让 WeekPager 完成布局，避免首次横滑才重置位置。回归包含 `TimetableExamTest` 与隔离包 `aiOnly` 仪器检查；`TimetableReturnChecks` 覆盖 AI 返回后的首次左右滑动。

- 聊天主界面顶部提供统一侧栏、独立临时聊天和菜单；强度通过输入框内轮盘选择。AI 主页面返回直接退出，课表右上角菜单不再重复提供 AI 入口；发送和停止共用居中的 ImageButton，强调色统一使用 PRIMARY；思考强度使用保持键盘的分档滑杆浮层，粉色填充跟随滑块，切换沿用 Motion 动效；加号菜单不夺取输入焦点或收起键盘。侧栏合并模块导航与项目聊天列表，点击顶部搜索才展开输入框，底部左侧新聊天、右侧头像设置；项目菜单进入独立项目列表与新建页。临时聊天仅存内存，不读取已有项目记忆，不写入聊天历史；切回普通聊天或进程结束后清除。语音输入使用系统 RecognitionService，先请求麦克风权限；识别结果只放入当前聊天草稿，离开页面或进后台取消并释放识别器。附件加号提供拍照、照片、文件与强度入口；手机信息和麦克风权限统一在设置 → 权限管理。菜单使用共享 MotionOverlay，左下方覆盖加号，关闭控件与原加号触控位置对齐；重复点击加号或收起键盘关闭；强度档位同步更新轮盘指针。图片直接上传 DeepSeek Files API，文件 ID 随多模态消息发送，上传在按发送时发生，失败可重试，远端文件 30 天自动过期。图片不再需要 OCR 插件；单图限 8 MB / 3200 万像素，文件读取限 10 MB；UTF-8 文本和 DOCX 提取正文（最多 64000 字），PDF 由系统渲染为图片（最多 4 页），每次最多 4 个手选附件或页面，独立于 2000 字输入框。聊天长按确认后可删除，保留项目记忆；最后一个聊天删除后补空聊天。所有主页面与二级页使用共享顶部边距，主页面大字号标题放到按钮下方；菜单不改变输入框行数。主题强调色统一为粉色，删除动作保留警示色。用户消息使用主题浅粉气泡，助手正文无背景框；标题按级别区分字号和粗细，正文、加粗与等宽代码分别呈现，流式与历史消息使用同一排版。对话滚动层铺满聊天页面，允许文字滚入顶部按钮和输入框后方；上下渐变保留透明度，不以实色区域截断正文，首尾仍留有完整阅读的滚动空间。未显示键盘时输入框收成单行胶囊，显示键盘后展开工具区；切换保留草稿与选区，菜单不改变展开状态。流式回复期间发送按钮切换为停止，取消后的工具建议不能执行；用户向上阅读时不强制滚到末尾。

- 普通对话使用流式文本，应用变更通过工具调用单独解析和确认；DeepSeek 强度分别对应关闭思考与 low/high/max，兼容协议仅在配置声明支持时传递 none/low/medium/high。聊天、项目和项目记忆保存在私有 `ai_chats` 偏好中，跨重启保留；每次请求使用最近32条消息及当前项目记忆，并由设备时钟即时提供日期、时间、星期、时区与偏移量。手机上下文另含机型、Android 版本、电量；日历与通知分别启用并取得系统授权后才在发送时读取，日历范围为昨天起未来 30 天 / 最多 100 条，通知仅当前最多 20 条且屏蔽验证码正文。独立 AI 通知监听器不复用或改变快递监听行为。MediaProjection 屏幕共享每次由系统授权，使用前台服务、持续通知和停止入口；只在内存保存最近外部画面，返回 Morrow 时暂停更新，发送问题时上传该快照并附捕获时间，停止共享清空帧，不自动重启或持久化投屏令牌。项目记忆可编辑、清空，模型可按用户授权自主追加、去重、替换和清空项目记忆，显示保存状态并提供一次撤销；写入前比较项目及记忆快照，临时聊天不保存长期记忆，凭据和验证码不得自动记忆。网络响应与工具参数仍在本机校验，不直接执行模型返回的文本。

- 底部 AI 与课表、任务、快递并列；跨模块请求提供当前本机任务和快递快照。任务操作沿用 TaskPage 的串行保存、账号隔离与恢复历史；快递沿用 ParcelRepository 的校验、去重、恢复、通知和组件更新。目标变化时拒绝写入。平台任务只变更本机记录，不代表向学习通提交作业。缺少手机读取授权时直接请求系统日历、通知或屏幕共享权限，返回后继续原问题，不跳转 Morrow 权限页。手机操作经确认后使用原生 Intent 打开已安装应用、设置、拨号器、闹钟、计时器，或调整媒体音量；不直接拨出电话，闹钟与计时器保留系统确认界面，不支持任意第三方界面点击，不让模型接触密码或密钥。
- API 配置支持最多 20 份供应商配置，包含可编辑的名称、HTTPS 基础地址、模型、协议、推理能力与密码遮罩下的原密钥，可新增、选择、编辑及确认删除。配置和密钥整体使用 Android Keystore + AES-GCM 加密并原子保存；首次从原 DeepSeek 密钥构造默认配置，持久化后保留其他配置，删除旧 DeepSeek 配置同时移除旧密钥。请求开始时固定所选配置，禁止跟随 HTTP 重定向，不向其他供应商转发已有远端文件 ID。DeepSeek 附件保留 Files API；兼容接口采用压缩图片 data URL，受现有聊天容量上限约束。参照 [DeepSeek Chat Completions](https://api-docs.deepseek.com/api/create-chat-completion/) 和 [OpenAI Chat Completions](https://developers.openai.com/api/reference/resources/chat)。
- 共享独立主操作占满内容宽度，成组的保存、删除、取消、恢复等操作使用 `SecondaryUi.actionRow` 横排并保留 48dp 触控高度；API 编辑、项目记忆、插件下载、快递读取与提醒复用该布局。聊天侧栏进入设置须记录 AI 来源，返回时复用原聊天及草稿；麦克风首次直接申请系统授权，再次拒绝的解释与永久拒绝的系统设置入口分开处理。
- 全局操作检查包括 `AiModulePlanTest`、`AiModulesIntegrationTest`、底部/侧栏导航测试；Windows 文件替换测试复用 `DataManagementTest.AtomicRename`。

- 目前接入 DeepSeek Chat Completions，以用户自行提供的 API 密钥认证；密钥用 Android Keystore 加密保存在 `deepseek_key` 中，可在助手设置中删除，不进入备份、日志、课表或对话。发送时将当前课表、有效单次调整、当前课表的手动考试记录、任务与快递数据及最近对话发给 DeepSeek；界面需明确告知和提示费用。
- 考试记录保存在对应课表偏好的 `aiExams`，日期必填，时间可未知；数据管理可单独清空。它不代表学校考试查询，也不自动同步。模型只能提出一个操作，所有添加、修改和删除都先展示并确认；应用校验字段、节次与日期，确认前检查当前课表和快照是否变化，保存失败时尝试恢复原数据。修改或删除课程可能使单次调整失效，须提示。
- 执行 `DeepSeekPlanTest`、`DeepSeekAssistantTest` 和相关界面测试；真实 DeepSeek 连接、密钥加密与设备键盘交互需另行验收。模型回复可能出错，不能把自然语言回答或确认前的建议当作已执行结果。

## 任务与学习通

- 账号验证成功后才使用 Android Keystore 加密保存凭据；密码不回填、不记录日志或导出。学习通接口只信任指定域名和标准端口，禁止跨域转发凭据；错误信息不暴露响应、地址、Cookie 或签名。
- 先读取本机缓存，刷新失败保留任务；网络响应须与最新本地数据合并，不能覆盖用户编辑、删除、恢复或设置。手动任务独立保存，切换账号不影响；课程关闭时隐藏其任务，重新开启可恢复。
- 缓存沿用 schema 1 兼容旧数据，并使用 AtomicFile 保存。按课程 / 作业标识合并；失败、分页未读和无法唯一对应的数据不删除，用户明确编辑或清除的截止时间不被覆盖。
- 按课程分组展示未完成任务，删除支持撤销和恢复，不改变平台提交状态。无年份的有效截止日期按当前年份计算倒计时，不修改原始数据；未知截止时间不显示时间行，开始时间不能充当截止时间。
- 修改任务功能时检查 `TaskDeadlineTest`、`TaskPageDesignTest`、`ChaoxingDeadlineTest` 和相关缓存、刷新测试；第三方平台适配还需实际账号验证，本地测试不代表平台端到端通过。

## 开发与构建

### 界面与目录约定

- 面向用户的名称统一为 **Morrow**，官网为 `https://morrow.xvyan.cn/`，仓库为 `fangfar/Morrow`；`cn.courselens` 包名、偏好键、签名、发布资产和工具参数保留兼容名称。
- `app/` 维护原生 Java/View 界面；`ocr-engine/` 供 `ocr-plugin/` 使用；`web/` 为官网源码，`remote/` 为发布配置；业务检查在 `tests/`，Android 单元与仪器检查分别在 `app/src/test/`、`app/src/androidTest/`。生成目录与验收截图不提交。
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
.\gradlew.bat --no-daemon -PqaBuild assembleDebug :app:assembleLiteDebugAndroidTest :ocr-plugin:assembleDebugAndroidTest :app:lintLiteDebug :ocr-plugin:lintDebug
```

Linux 使用 `python3` 和 `sh gradlew`。`run_checks.py` 是本地与 CI 共用的 Java 业务回归入口，另含本实验的学习通解析检查；Gradle 单元测试的 `NO-SOURCE` 不代表它们通过。

普通贡献者使用 `-PqaBuild`：Debug 包名为 `cn.courselens.qa`，使用开发签名；测试数据在测试 APK 中，Release 变体禁用。正式构建保持 `cn.courselens`，必须提供既有签名密钥并通过固定证书检查。

## 快递中心

### 主程序与识图插件

- 当前只维护原轻量版的主程序和独立 OCR 插件；保留 `lite` 构建名以兼容现有构建路径。主程序不包含 OCR 模型，识图时安装插件；包名、签名和数据格式保持不变。
- 发布草稿仅生成主程序和 OCR 插件。远程配置保留历史字段，供旧客户端使用；新客户端使用 `android.liteApkUrl`。公开发布后再更新下载配置。

### 可选图片识别插件

- `ocr-plugin/` 是可选安装的独立应用，正式包名 `cn.courselens.ocr`，QA 为 `cn.courselens.ocr.qa`。与主包使用相同签名配置，但独立安装、卸载和版本管理。主包不依赖 ML Kit、不包含 OCR 模型或原生库；通知、粘贴文字和手动录入无需插件。插件不声明网络权限，不上传截图。
- 「读取信息」和「识别设置」提供插件入口。主包从 `https://morrow.xvyan.cn/remote/ocr-plugin.json` 读取 HTTPS 下载地址、字节数、SHA-256、插件版本和协议。未发布时 `enabled: false`；不使用占位 APK 地址。下载交给系统 DownloadManager，界面显示进度、等待网络和失败重试，离开页面后可继续下载，再次进入恢复进度。下载后复制到私有目录，校验大小、SHA-256、包名、主包签名和协议 1，再临时授权系统安装器读取该 APK；取消下载会清除任务和安装缓存。QA 不下载正式插件，需安装同签名 QA 产物。
- Android 首次安装需用户允许 Morrow 安装应用，并在系统安装器确认；不静默安装。安装返回后重新检查插件并继续选择 / 识别图片，取消或拒绝授权可重试。插件只接受同签名且包名匹配的主包调用，读取单个临时 `content` URI，返回有长度和坐标边界的文本行，由主包沿用 `ParcelRules.extractImage` 提取快递信息；不传整张位图或保存截图。
- `.\gradlew.bat --no-daemon -PqaBuild assembleDebug :app:assembleLiteDebugAndroidTest :ocr-plugin:assembleDebugAndroidTest :app:testLiteDebugUnitTest :app:lintLiteDebug :ocr-plugin:lintDebug` 同时检查主包和插件。`python tests/OcrPluginChecks.py --host app/build/outputs/apk/lite/debug/app-lite-debug.apk --plugin ocr-plugin/build/outputs/apk/debug/ocr-plugin-debug.apk` 检查同签名、包名、模型隔离和插件无网络权限。正式发布工作流额外生成插件 APK 和 `ocr-plugin.json` 至草稿产物；公开插件后才把已验证的描述文件通过 PR 更新到 `remote/ocr-plugin.json`。下载地址可使用经审核的 HTTPS 国内对象存储，内容必须与校验值一致。准备产物不代表上传、公开发布或下载链路验收。
- QA 下载测试可额外使用 `-PqaOcrManifest=http://127.0.0.1:8765/ocr-plugin.json`，通过 `adb -s SERIAL reverse tcp:8765 tcp:8765` 连接仅监听电脑回环地址的临时文件服务。描述文件必须指向 `cn.courselens.ocr.qa` 同签名插件；正式包拒绝 QA 插件及 HTTP 地址。只有 debug 网络配置放行精确 `127.0.0.1`，不放行局域网域名，也不部署 QA 下载配置到官网。安装 QA 主包和测试包后，可运行 `adb -s SERIAL shell am instrument -w -e ocrDownloadOnly true cn.courselens.qa.test/cn.courselens.NativeChecks` 启动下载专项；系统安装授权与确认仍需在手机界面完成，专项不打开业务页面或写入快递数据库。测试完删除 ADB 转发并停止服务，使用此测试包继续下载前需重新建立测试源。
- 发布前验收：未安装入口、下载失败 / 取消 / 网络恢复、进后台 / 进程重建后恢复、损坏及异签名文件拒绝、拒绝安装权限、取消安装、安装成功后识别、卸载回退、图片分享、协议不兼容、深色 / 大字体。设备测试使用合成截图和隔离 QA 数据。


- 入口在底部导航的「快递」，也可由 Android 文字 / 图片分享打开。三个底栏入口在主界面与快递页共用 `AppNavigation`，选中使用实心图标；返回课表 / 任务时复用主 Activity、保留原页面和相应选中项，并清除其上的快递页，避免返回键循环。独立 `ParcelActivity` 使用现有 `SecondaryUi`、原生 View 和半高滚动表单，头部菜单与添加按钮共用全应用 `SecondaryUi` 圆形图标样式，浅色沿用原色板，浅色与深色均复用共享色板。没有迁移课表、学习通缓存或账号数据，也没有改包名、版本号或正式签名。
- 快递页使用单一快递列表，不按状态或驿站分类，不左右翻页。点击条目内容内联展开编辑、删除、复制取件码、提醒及已有物流记录，同一时间只展开一条；展开后的「已取件」按钮标记完成，完成后保留在同一列表末尾，地址与取件码使用灰色、取件码加删除线，不另显示完成状态文字；提供 5 秒撤销和展开后的「恢复未取件」，恢复不自动重启旧提醒；既有已取件记录同样可见。未核对的识别结果在同一列表提示核对，确认前不能标记完成。顶部保留淘宝 / 菜鸟 / 拼多多官方身份码入口；「手动添加」直接打开表单，「读取信息」提供粘贴文字、选择截图及直达通知读取设置。选择图片后关闭读取面板，离线识别不显示处理中弹窗，候选直接进入列表待核对；识别失败或无结果显示反馈，可重新选择图片或手动添加。旧版 OCR 插件仍可能显示处理中界面，安装同签名 1.0.1 或更新版本后才会消除。填写取件码或运单号即可保存，沿用原有状态字段及去重规则；不提供在线物流查询。全部数据删除仍在总设置 → 数据管理。

- `ParcelDatabase` 是独立 Room schema 2 数据库 `parcels.db`，schema 保存在 `app/schemas/`。Java 使用 annotationProcessor，无 Compose / Hilt / Kotlin 插件；Room 2.6.1、WorkManager 2.9.1 保持现有 SDK 34 / JDK 17 构建兼容。升级 schema 时添加保留数据的 migration，不使用 destructive fallback。取件码和运单号分别保存；同运单或同地址同码去重，同码不同驿站保留。完成记录不因通知复活。运输中记录收到同运单取件通知时补全原记录，低评分信息需确认。单条删除移入本机快递恢复，并保留最长 14 天的 SHA-256 去重摘要；可从快递菜单或数据管理恢复，恢复不重启提醒，删除全部会清除记录、摘要和设置，并执行 Room 的 WAL checkpoint / VACUUM。
- `ParcelRules` 是本地版本化规则（当前 v2），评分是规则强度而非统计概率。只提取明确取件语境，排除验证码和动态身份码。只有单码、明确地址的高评分通知可以直接进入待取件；其他结果在快递列表中标记为待核对，由用户人工核对，截图 / 分享一律确认。通知 / 粘贴多码文本不自动配地址或运单，避免串件；截图按 ML Kit 文本行坐标关联标签、码值和附近取件点，同一标签含多码时仍留空地址待补。截图局部区域排除身份码 / 验证码，不因整屏其他位置出现无关安全词而丢弃有效取件码。没有保存通知全文或图片，没有短信、无障碍、相机或媒体库读取权限。截图使用文档选择器 / 分享授权和独立可选插件内的中文 ML Kit；只接收 content URI，限 10 MB / 8000 万输入像素并下采样处理，识别后释放图片和识别器。
- 通知识别默认关闭，需 App 内同意和系统通知访问两步授权；已授权时再次开启不重复跳转系统页面，返回后更新授权状态。开启后自动识别菜鸟、淘宝、拼多多、微信、QQ 及默认短信应用的**通知**，不需要选择来源；系统未返回默认短信应用时，已安装的 `com.android.mms` 可作为系统短信来源。不访问短信数据库。`ParcelNotificationListener` 由系统绑定权限保护，不扫描历史通知、不启动前台服务保活。关闭或清空数据会使排队中的旧识别结果失效。通知显示权限和读取通知访问相互独立。Android 15 及以上的敏感通知过滤、侧载应用受限设置、工作资料隔离和厂商限制都可能导致通知正文不可见；提供手动 / 分享入口，不绕过系统限制。
- `ParcelIdentity` 只打开包限定的官方路由，捕获跳转失败后尝试打开应用首页，不弹提示。深链成功发出 Intent 不证明已到身份码页面。拼多多入口通过 `tab=ID_CODE&entry_source=11` 指定身份码页签，已在小米 14 / Android 16 的拼多多 8.3.0 验证；淘宝、菜鸟及其他版本仍需分别验收，不据此称为当前所有版本有效。页面不生成、读取、抓取或缓存官方动态令牌。
- 在线物流查询与代理配置已移除。打开快递页时取消旧版 `parcel-tracking` 后台任务，并清除旧代理、后台查询开关与退避设置；旧版已排队的查询任务直接结束。保留已有物流字段，只供本机记录展示与旧数据兼容，不再发出物流查询请求。
- 新识别和自定义未领取提醒共用独立通知频道，默认提醒关闭；Android 13 起只在用户开启提醒时请求 `POST_NOTIFICATIONS`。通知不包含码值和地址，按记录替换，识别去重避免重复推送；完成 / 删除取消通知。未领取提醒用 WorkManager 每 15 分钟检查。无精确闹钟 / 前台保活，不承诺准点；强行停止、Doze、重启和厂商省电均需实机检查。关闭开关取消对应周期任务。
- `ParcelWidget` 独立于课表小组件，支持系统固定请求与手动添加。默认只显示待取数量；显示取件码与地址需在快递设置中主动开启。数据库修改后刷新，支持深色资源和 sp 字号。真实启动器添加、尺寸、大字体、点击与隐私开关需实机验收，不能以 `requestPinAppWidget` 返回值充当添加成功。

### 参考来源与许可边界

2026-10-25 重新核查了下列固定来源。取件识别采用可选插件内的 ML Kit 中文 OCR 与 `shareven/parcel` 的 MIT 取件标签、地址规则局部适配，未整体合并第三方应用。已在源码标注来源，并在 `app/src/main/assets/licenses/shareven-parcel.txt` 保留上游版权和许可全文；第三方许可不改变 Morrow 本身的许可状态。方案比较见 [取件识别调研](docs/parcel-recognition-research.md)。

| 来源 | 核查文件与兼容性 | 使用方式 |
| --- | --- | --- |
| [shareven/parcel @ d7e4c97](https://github.com/shareven/parcel/tree/d7e4c974ffd1e4de2da9291df0d295856cd89818) | MIT；`util/SmsParser.kt`、`util/ThirdPartyEntry.kt`、`service/ParcelNotificationListenerService.kt`、`app/build.gradle.kts`；Kotlin / Compose，minSdk 29 / compileSdk 35 | 适配明确取件标签与地址前缀，保留 MIT 声明；不移植其短信权限、保活或完整 UI |
| [pickup-code-app @ f5075d3](https://github.com/zixij644-elaborate/pickup-code-app/tree/f5075d3e4d2f4ffe4da259c699f472a790ec7013) | GPL-3.0；`extractor/CodeExtractor.kt`、`util/IdentityCodeLauncher.kt`、Gradle；Compose，minSdk 26 / compileSdk 35 | 仅参考确认流程、来源评分和包限定路由事实；未复制或翻译 GPL 实现 |
| [ExpressOCR @ b1aad68](https://github.com/baso4/ExpressOCR/tree/b1aad68b51bf8dc4b2bdead655489a99691647c1) | MIT；`MainActivity.kt`、`CameraActivity.kt`、Gradle；CameraX / ML Kit，minSdk 26 / compileSdk 35 | 参考截图 → 本机 ML Kit 的流程；直接使用官方 SDK，自行处理大小限制和释放；不采用敏感日志、相机扫描 |
| [KdAPI_Demo @ 603063c](https://github.com/APIGI/KdAPI_Demo/tree/603063c8a8e0a2acbf75f4de23def39f78982711) | 未找到 LICENSE；`2-Services/OrderService.cs`、README；.NET Core 3.1，不能直接作为 Android 库 | 仅核对服务端签名 / 查询职责，未移植代码、示例凭据或签名算法 |

Android 实现依据：[Room Java 配置](https://developer.android.com/training/data-storage/room)、[ML Kit 中文识别](https://developers.google.com/ml-kit/vision/text-recognition/v2/android)、[NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService)、[Android 13 通知权限](https://developer.android.com/develop/ui/views/notifications/notification-permission)、[Android 15 行为变化](https://developer.android.com/about/versions/15/behavior-changes-all)、[WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started)。这些依赖有各自许可和条款，不代表 Morrow 采用相同项目许可。

### 快递测试

```powershell
python tests/run_checks.py
.\gradlew.bat --no-daemon -PqaBuild assembleDebug :app:assembleLiteDebugAndroidTest :ocr-plugin:assembleDebugAndroidTest :app:testLiteDebugUnitTest :app:lintLiteDebug :ocr-plugin:lintDebug
adb -s SERIAL shell am instrument -w -e parcelOnly true cn.courselens.qa.test/cn.courselens.NativeChecks
```

`ParcelRulesTest` 使用合成语料及 OCR 行坐标，覆盖标签别名、换行、多码、复制按钮干扰、同屏身份码和电话 / 日期排除；主包的 `parcelOcrOnly` 仪器专项验证插件结果协议与人工确认边界，不再调用识别模型。真实中文识别模型由独立 QA 插件专项验证：`adb -s SERIAL shell am instrument -w cn.courselens.ocr.qa.test/cn.courselens.ocr.OcrChecks`，只生成合成图片、不写快递数据库；该项不能代替主包下载 / 安装 / 回传的端到端验收。`ParcelAndroidTest` 用 Robolectric 模拟 Android 13 / 14 / 15，检查 Room 去重、运输中补全、删除/完成防复活、权限开关、旧查询任务取消与提醒保留。Android 16 需 JDK 21，可使用 `-PparcelTestSdks=33,34,35,36`；默认 JDK 17 不运行 API 36，不把未运行版本算通过。`ParcelChecks` 仪器专项使用唯一临时数据库，测试包名必须为 QA；设备安装和备份仍按「设备验证」执行。CI 运行 JVM / Robolectric 检查并保留报告，不运行设备测试。

每个可用 Android 13 / 14 / 15 / 16 及以上的 QA 设备还需检查：首次拒绝/允许通知、频道关闭、通知访问授权/撤销与重新绑定、后台识别关闭后无写入；不同来源/重复通知/敏感通知过滤；分享 grant、过大图片、截图 OCR 和多码确认；深色及 2 倍字体；Doze / 重启 / 强行停止后的提醒延迟和提醒开关；真实启动器固定请求、尺寸、取件后刷新和隐藏码值。无设备时只能交付构建及模拟结果，不能宣称这些端到端检查通过。64 位 ML Kit 原生库的 16 KiB ELF / APK 对齐检查也不能代替 Android 15+ 16 KiB 页设备运行。

## 多份课表与学校适配

- 当前课表 ID 和名称列表放在 `timetables` SharedPreferences；原有课表继续使用 Activity 原偏好文件，不复制或改写已有课程格式。设置中的当前课表与切换列表支持长按名称改名，名称校验沿用 1–40 个字；只更新索引，不搬移课程数据。从设置进入新增页，取消或手动创建后返回设置；首页入口返回课表页。新增课表使用独立偏好文件，课程、学期、备注、冲突选择、调课与 `classTimes` 随课表切换。小组件读取同一当前 ID，旧课表的小组件点击不会误匹配新课表。学习通账号和作业仍使用原有独立存储。
- `ClassTimes` 以每行 `HH:mm-HH:mm` 保存作息，1–30 节，检查顺序、重叠与课程节次上界。作息页按节次显示上课 / 下课时间，使用 24 小时选择器，支持新增一节与移除末节，并沿用保存 / 放弃草稿提示。未保存该字段的旧课表沿用宿迁学院 12 节作息。新增可手动创建或选择学校导入；徐州工程学院通过个人课表接口读取已选课程及各校区日节次，支持分段周次、单双周、单周调课与无时间实践备注。不同校区作息不一致时明确拒绝合并，保留旧课表。
- 徐工院仅放行 `jwglxt.xzit.edu.cn` 的标准 HTTP/HTTPS 端口，禁止跨域或 URL 用户信息，密码使用学校 RSA/PKCS1 协议；登录成功后按课表使用 Android Keystore 加密保存账号密码，供静默刷新，不保存 Cookie。Android 网络配置只对该精确域名允许 HTTP，不包含子域，其余流量保持 HTTPS。该校当前 HTTP 服务不提供传输完整性和会话保密性，登录页及 README 明示限制；HTTPS 可用后应移除此例外。
- 使用 `-e timetablesOnly true` 运行多课表与徐工院合成专项，涵盖旧数据保留、新建与切换、重建恢复、14 节编辑与小组件、RSA/解析及域名边界；测试只使用临时合成课表，结束恢复索引。真实账号读取只验证第三方链路，不把私人课表或凭据放入测试资产。
- 学校课表同步逻辑保留，课表管理页不显示刷新入口；`schoolImported` 随各课表保存，旧数据通过学校同步备注识别。每个课表绑定各自学校账号：登录成功后使用 Android Keystore 加密保存，刷新先复用当前会话，会话过期或进程重启后读取凭据静默登录；验证码、凭据失效或学校不匹配时进入登录页，账号可预填、密码不回填。清空当前课表时删除该课表保存的账号。仅刷新成功后覆盖课表；失败、无效数据或页面已切换时保留原课表。`timetablesOnly` 同时覆盖刷新入口、后台读取、成功保存、失败保留、静默登录、验证码回退和凭据存取。

## 设备验证

先核对 `adb devices -l` 的目标序列号，备份 QA 包已有偏好。只覆盖安装 QA 主包和测试包，不卸载用户正式包。

```powershell
adb -s SERIAL install -r app/build/outputs/apk/lite/debug/app-lite-debug.apk
adb -s SERIAL install -r app/build/outputs/apk/androidTest/lite/debug/app-lite-debug-androidTest.apk
adb -s SERIAL shell am instrument -w -e saveOnly true cn.courselens.qa.test/cn.courselens.NativeChecks
```

`SERIAL` 替换为已核验序列号，手机保持解锁和 App 前台。参数可换为 `chatMotionOnly`（合成聊天草稿、真实键盘升降、输入框连续高度、附件菜单与设置权限入口）、`tabStartupOnly`（冷进程下 54 份合成缓存作业的首次 / 后续底栏点击、首帧及主线程停顿）、`capsuleOnly`（冷启动恢复非零筛选、加载布局变化后的首次滑动、反向取消、点击动画及底栏无水波纹）、`chaoxingOnly`（学习通只读客户端、课程开关、删除恢复与刷新保留的合成检查）、`navigationOnly`（任务页导航、紧凑操作及普通 / 大字体布局）、`choiceOnly`（课程与调课选择器、取消和草稿保留）、`dialogLayoutOnly`（普通与大字体弹窗正文、按钮分区）、`courseSlotsOnly`、`scrollOnly`、`conflictOnly`、`adjustmentOnly`、`widgetOnly`、`widgetGuideOnly`、`widgetPinOnly`、`motionOnly`、`gestureOnly`、`performanceOnly` 或 `remoteOnly`；不指定专项参数运行全量入口。`nextClassOnly` 是历史名称，首页已无下一节提示。

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

本地正式构建按 `signing.properties.example` 配置被忽略的签名属性，密钥保存在仓库外；执行 `python tests/SigningChecks.py --signed`、正式 `assembleRelease :app:lintLiteRelease :ocr-plugin:lintRelease`。`python tests/ReleaseChecks.py vVERSION --apk PATH --edition lite`使用 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT` 检查产物，输出到 `app/build/release/`，不发布。

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

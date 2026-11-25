# Southside 3.0.0 Fullsrc

从 `southside-3.0.0.jar` 字节码直接反混淆、清理假控制流、还原符号并反编译重建的 Minecraft 1.21.4 (Fabric) 完整源码工程，可以直接用 Gradle 编译出可用的 jar。仓库里也附带了 `southside_auth.dll` 的 C++17 功能重建源码和完整的混淆符号映射表。

## 原版 southside-3.0.0.jar 混淆赏析

刚拿到这个快 50 MB 的 `southside-3.0.0.jar` 的时候，我还以为里面藏了什么航天级防破解黑科技，结果拆开一看简直是正宗老狗屎，整个保护方案主打一个集成百家之短，槽点密到不知道先笑哪一个。

先说体积。原版 jar 压缩后 46.62 MB，解压完 68 MB，但里面全部 982 个 `.class` 字节码文件打包完一共才 5.55 MB，连总包的零头都算不上。剩下四十多兆全是什么东西？一个套了 VMProtect 灌水膨胀到 16.76 MB 的登录弹窗 DLL，两套连字集都懒得裁剪的全字库 `regular.ttf`（12.49 MB）和 `semibold.ttf`（12.40 MB），外加 4.41 MB 的文泉驿微米黑。最抽象的是 `assets/southside/stickers/sweat.gif`，一个流汗黄豆动图居然塞了 5.37 MB，单个表情包快赶上整个客户端所有 Java 代码的体积了。

再看 Java 侧的混淆。原版走的是极其标准甚至有点复古的 ZKM 流水线，把 879 个业务类全拍平扔进默认包，类名全换成 24 位 `I` 和 `l` 拼出来的条形码，比如客户端入口类叫 `IllIllIIIIlIIlIIIllIIlII`。这种把戏对脚本和反混淆工具来说跟没改名没区别，唯一的作用就是折磨盯着反编译器裸看的人的视神经。而且因为 Fabric 不允许 Mixin 放在默认包，103 个 Mixin 类只好乖乖挪进 `ma5b715` 包里，顺便完整附赠了一份 77 KB 明文的 `southside-refmap.json` 和 `southside.accesswidener`，每一个 Mixin 注入的是 Minecraft 哪个类哪个方法哪个字段写得明明白白，连客户端入口类都在 `fabric.mod.json` 里直接指路。

但全篇最令人蒙古的操作还是字段重命名。ZKM 辛辛苦苦把全项目的字段名都搅成了 `lIlIIlIIlIllIlllIIlllIII` 这种鬼画符，结果作者大概是怕混淆完 Gson 序列化配置炸掉，直接给全项目 508 个类（982 个类里超过一半）的字段全贴了明文的 `@SerializedName("原字段名")`。贴给配置类也就算了，主类里连 `eventBus`、`moduleManager`、`commandManager`、`configManager`、`friendManager`、`scheduler`（`ScheduledExecutorService`）、`shutdownStarted`（`AtomicBoolean`），还有认证类里的 `receiveLock`、`transportLock`、`nativeHandle`、`publicHttp`、`privateHttp` 这种压根不可能拿去 JSON 序列化的运行时对象，头顶上全顶着 `@SerializedName` 霓虹灯牌写着自己的真名。我不太理解这是什么操作逻辑但只能尊重一下了，相当于花大价钱买把防盗锁把门锁死，然后把钥匙用胶带粘在门把手上，旁边还贴了张便利贴写着钥匙在这。再加上 645 个类保留了 `RuntimeVisibleAnnotations`，359 个类保留了泛型 `Signature`，52 个枚举常量原封不动，连根目录的 `auth-ca.pem` 证书都明文躺着，语义信息基本等于全盘白送。

字符串加密和控制流也是 ZKM 祖传制式模板，没有任何自研虚拟机也没有改过一行算法。701 个类清一色的 `String[] a/b/f`、`long[] c`、`invokedynamic` 传个 `long` 常数，外加 `sipush` 配 `a:(II)Ljava/lang/String;`；控制流全靠类里一个恒定的静态 `int` 或 `boolean` 开关到处插 `ifeq` 和 `ifne`，顺便往异常表里塞满永远不会触发的 `invokestatic a:(Exception)Exception; athrow`。这种纯制式写法直接静态模拟跑一遍，字符串和调用目标 100% 无损出来，死分支一剪，控制流瞬间干净得跟原版源码一样。

最后压轴的还得是那个 16.76 MB 的 `southside_auth.dll`。为了保护登录验证，作者给一个普通的 C++ Dear ImGui 登录窗口加 WSS 客户端套上了 VMProtect 壳，把本来 1.71 MB 就能写完的东西硬生生膨胀了快十倍。但你翻开调用它的 Java 类 `IIIIllllIIlIIIIlllllIlIl` 一看，10 个 JNI `native` 方法名 `create0`、`showLogin0`、`reconnect0`、`onOpen0`、`receive0`、`disconnected0`、`authenticated0`、`release0`、`chat0`、`presence0` 全是明文裸奔。最让人绷不住的是入口 `onInitializeClient()` 里怎么把关的：

```text
25: invokestatic  #873 // Method IIIIllllIIlIIIIlllllIlIl.lIllIlIlllIlIIIlIlIllIll:()Z
28: ifne          75
```

辛辛苦苦花 16.76 MB 给 DLL 筑了座 VMP 紫禁城，结果城门口就挂了把 1 字节的塑料插销，Java 侧改一个字节或者删掉三行调用，这 16.76 MB 的铁壁连加载的机会都没有。什么？你说万一后面的核心功能依赖 DLL 里的校验状态呢？那没事了，压根没依赖。

## Fullsrc 目录与内容说明

整个工程包含 743 个 Java 源文件，完整还原了 982 个类、3,732 个字段和 5,851 个方法，另外附带重建后的 C++17 原生认证模块源码。Java 启动流程里的认证调用已经默认去掉，保留了 JNI 接口和打包结构方便自己研究或二开。

```text
Fullsrc/
├── build.gradle                     Fabric Loom 1.15.5 / Minecraft 1.21.4 构建脚本
├── settings.gradle                  Gradle 项目与插件源配置
├── gradle.properties                JVM 内存和 Gradle 缓存配置
├── gradlew.bat                      Windows Gradle Wrapper
├── gradle/wrapper/                  Gradle 9.2.1 Wrapper
├── mappings/
│   ├── full-symbol-map.tsv          原版混淆符号到还原符号的完整映射表（10,568 行）
│   └── yarn-1.21.4+build.8-v2.jar   Minecraft 1.21.4 Yarn Tiny v2 映射本地归档
├── native/
│   └── southside_auth/              C++17 重建的原生认证模块源码、依赖和构建脚本
└── src/
    └── main/
        ├── java/
        │   ├── dev/southside/       客户端核心源码（709 个文件）
        │   └── cc/polyfrost/oneconfig/  内置 OneConfig UI/配置/HUD 框架（34 个文件）
        └── resources/
            ├── fabric.mod.json      Mod 元数据，入口指向 dev.southside.Southside
            ├── southside.mixins.json
            ├── oneconfig.mixins.json
            ├── southside.accesswidener  Yarn 命名空间 v2 AccessWidener
            ├── auth-ca.pem          认证服务端 CA 证书
            ├── assets/              贴图、字体、着色器、贴纸和音频资源
            ├── META-INF/jars/       内嵌依赖（jlayer、lwjgl-nanovg 及 Windows natives）
            └── native/windows-x86_64/southside_auth.dll  重建版原生 DLL（1.71 MB）
```

### Java 包分布

- `dev.southside` (1)：主入口 `Southside`，负责初始化事件总线、模块/命令/配置/好友/贴纸管理器以及渲染上下文
- `dev.southside.module` (140)：模块基类 `Module`、`Category`、`ModuleManager`、配置值系统及 7 大分类 135 个功能模块
- `dev.southside.mixin` (103)：针对 Minecraft 1.21.4 客户端渲染、移动、发包、交互、实体和 HUD 的全部 Mixin
- `dev.southside.event` (46)：事件总线 `EventBus` 与各类游戏周期、渲染、移动、输入、发包事件
- `dev.southside.render` (36)：OpenGL 着色器管线、`NanoVGContext`、Mesh/ShaderFactory、进度条覆层与 2D/3D 渲染
- `dev.southside.command` (19)：命令解析器及 10 个内置命令（`Bind`、`Configs`、`FakePlayer`、`Friend`、`Goto`、`IRC`、`Report`、`Stickers`、`Test`、`Toggle`）
- `dev.southside.gui` (7)：客户端 GUI 界面与交互组件
- `dev.southside.auth` (6)：`NativeWebsocketClient`（JNI 桥接）、`IrcClient` 及连接状态定义
- `dev.southside.manager` (4)：`ConfigManager`、`FriendManager`、`IRCManager`、`StickerManager`
- `dev.southside.discordipc` (4)：Discord Rich Presence IPC 通信
- `dev.southside.anticheat` (2)：客户端侧反作弊检测辅助
- `dev.southside.nanovg` (1)：`NanoFontRenderer` NanoVG 字体渲染器
- `dev.southside.nel` (1)：NEL 网络扩展辅助类
- `dev.southside.utils` (339)：转头管理、移动/发包/背包/攻击/床与箱子追踪、Heypixel 适配、AI 寻路、OneConfig 桥接等工具类
- `cc.polyfrost.oneconfig` (34)：内置精简版 OneConfig UI、配置注解和 HUD 框架

### 内置模块列表

- Client (9): `ArrayList`, `ClientSettings`, `MCF`, `Notification`, `Renders`, `TabGUI`, `Targets`, `Theme`, `Watermark`
- Combat (20): `AntiFireball`, `AutoClicker`, `AutoCrystal`, `AutoKnockBack`, `AutoRod`, `BackTrack`, `BalanceHitSelect`, `BedAura`, `BowAimAssist`, `BowSpammer`, `Criticals`, `FeetCity`, `GrimFull`, `HitBox`, `KBController`, `KillAura`, `SuperKnockBack`, `TimerRange`, `TriggerBot`, `Velocity`
- Misc (21): `AntiBot`, `AntiExploit`, `AntiNick`, `AntiStaff`, `AutoGApple`, `AutoPixelParty`, `AutoPlay`, `BedwarsHelper`, `BlockHunter`, `ChatBypass`, `ClientSpoof`, `Disabler`, `DiscordPresence`, `FakeLag`, `FuckOffHand`, `GodItemDetector`, `HackerDetector`, `KillSay`, `MatchingBypass`, `MurderFinder`, `Teams`
- Movement (16): `FastWeb`, `Flight`, `GrimFly`, `InventoryMove`, `KeepSprint`, `LongJump`, `LongJump2`, `NoJumpDelay`, `NoPush`, `NoSlow`, `Speed`, `Spider`, `Sprint`, `StrafeFix`, `TP2Bed`, `Timer`
- Player (21): `AntiVoid`, `AntiWeb`, `AutoBucket`, `AutoOffhand`, `AutoPot`, `AutoSoup`, `AutoTool`, `AutoTotem`, `AutoTrap`, `Blink`, `BlockIn`, `Eagle`, `GhostHand`, `HealthBypass`, `InvManager`, `KeyPearl`, `NoFall`, `NoteBot`, `QuickbarManager`, `Stealer`, `Stuck`
- Render (40): `BedShow`, `BrokenClouds`, `Chatting`, `ChestESP`, `Compass`, `CriticalParticle`, `CustomFOV`, `Detector`, `ESP2D`, `FakerVSBin`, `FullBright`, `Glow`, `HUD`, `InventoryHUD`, `IronESP`, `Item2D`, `ItemHighlight`, `ItemHud`, `ItemPhysics`, `ItemTag`, `KillEffect`, `LineBreakFormattor`, `MotionBlur`, `MotionCamera`, `NameProtect`, `NameTags`, `NoRender`, `OldHitting`, `PaperDoll`, `PlayerAppearance`, `PotionEffects`, `RainLove`, `RenderTest`, `ScoreBoard`, `TNTips`, `TargetHUD`, `TimerChange`, `Trajectories`, `ViewClip`, `WorldManager`
- World (8): `AntiLiquid`, `ChestAura`, `ChestWreck`, `EntityEditor`, `FastPlace`, `LightningTracker`, `Scaffold`（含 `sca/Line`）

### 原生认证模块 (`native/southside_auth/`)

基于原版 DLL 逆向分析重建的独立 C++17 工程，详见 `native/southside_auth/README.md`：

- `src/southside_auth.cpp` / `southside_auth.def`：JNI 导出函数和 Java 回调
- `src/session.cpp` / `session.hpp`：登录 (`t=1`)、注册 (`t=4`)、充值 (`t=5`)、在线状态 (`t=6`)、聊天 (`t=7`) 协议与 WSS 重连状态机
- `src/crypto.cpp` / `crypto.hpp`：SHA-256/Base64 请求签名、设备标识生成以及 AES-256-GCM 凭据文件 (`SouthCredentials.dat`) 加解密
- `src/login_window.cpp` / `login_window.hpp`：Win32 + DirectX 11 + Dear ImGui 登录/注册/充值窗口
- `vendor/`：内嵌 `imgui` 与 `nlohmann/json` 源码
- `CMakeLists.txt` / `build.py` / `build.bat`：支持 CMake 或 Zig C++ 工具链编译

## 构建说明

环境要求：Minecraft 1.21.4、Fabric Loader 0.18.1、Fabric API 0.119.2+1.21.4、JDK 21+（配置好 `JAVA_HOME`）。

编译 Mod：

```powershell
.\gradlew.bat clean build --no-daemon
```

产物输出在 `build/libs/southside-3.0.0.jar`。

如果需要重新编译原生 DLL，确保已设置 `JAVA_HOME` 且环境中可用 Zig 0.14+（加入 `PATH` 或设置 `ZIG` 环境变量）：

```powershell
cd native/southside_auth
.\build.bat
```

也可以直接用现有的 C++17 工具链（MinGW / MSVC）配合 `native/southside_auth/CMakeLists.txt` 构建。

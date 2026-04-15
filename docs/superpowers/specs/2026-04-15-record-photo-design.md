# Record Photo Support Design

## Goal

为每条日记记录增加最多一张照片的能力，支持相册选择和直接拍照，照片仅在单条记录中展示缩略图，点击后查看大图；照片保存在本地，支持备份导出导入，并随现有 S3 备份同步机制一起工作。

## Scope

本次设计覆盖：

- 首页新建记录时选择一张照片
- 首页和回顾页的记录卡片展示缩略图
- 点击缩略图查看大图
- 编辑记录时替换已有照片，直接覆盖旧图
- 删除记录时联动删除本地照片文件
- 备份导出导入时包含记录照片
- S3 同步通过现有备份包机制携带记录照片

本次明确不做：

- 每条记录多图
- AI 日记中嵌入图片
- 图片缩放、旋转、分享、批量管理
- 纯图片记录；记录仍要求文字必填
- 脱离备份包的单独图片对象同步协议

## User Requirements

### Record rules

- 每条记录最多允许一张照片
- 记录必须有文字内容，只有图片不能保存
- 编辑已有记录时，如果重新选择相册图或重新拍照，新图直接覆盖旧图

### Display rules

- 图片只在单条记录中展示，不进入 AI 日记内容
- 首页和回顾页都展示小图缩略图
- 点击小图后进入大图预览层

### Backup and sync rules

- 照片保存在本地
- 导出备份时包含照片
- 导入备份时恢复照片
- S3 同步沿用现有备份包同步机制
- 图片冲突跟随现有冲突策略：`替换冲突项` 时覆盖本地，`合并` 时保留本地

## Architecture

整体方案采用“记录自带单图字段 + 应用私有目录图片文件”的轻量实现。

数据层继续以 `Record` 为核心，不增加附件表。每条记录新增一个 `photoPath` 字段，保存应用私有目录中的正式图片文件路径，而不是系统相册 URI。相册和拍照输入都先转成应用内部压缩后的单张 JPEG，再更新记录。

同步层不引入新的媒体协议。记录图片跟随现有备份 zip 一起导出、导入和上传 S3，这样可以复用当前数据管理、导入预检和冲突处理模型，避免把简单功能扩展成独立媒体系统。

## Data Model

### Record model

在以下结构中新增可空图片路径字段，默认空字符串：

- `app/src/main/java/com/aiawareness/diary/data/model/Record.kt`
- `app/src/main/java/com/aiawareness/diary/data/local/RecordEntity.kt`

新增字段：

- `photoPath: String = ""`

设计约束：

- `photoPath` 仅保存应用私有目录中的本地绝对路径
- 不保存系统返回的 `content://` URI
- 不保存临时拍照文件路径

### Database migration

`records` 表需要新增 `photoPath` 列，默认值为空字符串。

迁移要求：

- 旧记录自动兼容，无图记录保持空字符串
- Room 实体与 DAO 查询继续以整行读写为主，不额外拆附件关系

## Local Photo Storage

新增专用存储组件，例如：

- `RecordPhotoStorage`

职责：

- 从系统相册 URI 导入图片
- 从拍照结果文件导入图片
- 压缩并保存为正式记录图片
- 替换旧图时删除旧文件
- 删除记录时清理对应图片文件

目录建议：

- `files/record_photos/`
- 临时拍照文件可使用 `cache/record_photo_capture/`

文件策略：

- 正式图片统一转成 JPEG
- 文件名使用时间戳或随机 ID，避免冲突
- 一条记录最终只保留一张正式图片

压缩策略：

- 读取原图后按最长边做缩放
- 压缩到适合日记查看的尺寸，不保留原始大图
- 目标是显著降低本地占用、导出包体积和 S3 同步成本

替换规则：

1. 新图先压缩并写入新正式文件
2. 记录更新为新的 `photoPath`
3. 旧图文件在新图成功后删除

这个顺序确保覆盖过程失败时不会把旧图丢掉。

## Capture and Pick Flow

### Album selection

- 设置 `ActivityResultContracts.OpenDocument()` 或等效相册选择器
- 读取选中的系统 URI
- 交给 `RecordPhotoStorage` 导入并压缩
- 最终只把正式本地路径写入记录

### Direct camera capture

- 使用 `FileProvider` 为相机预分配临时输出文件
- 相机写入临时文件后，交给 `RecordPhotoStorage` 压缩为正式图片
- 成功后删除临时文件
- 取消或失败时清理临时文件

这样可以避免直接把原始大图长期保存在可见目录中，也避免把临时拍照路径持久化到记录里。

## UI and Interaction

### Home input area

首页输入区域增加一个图片入口按钮。

点击按钮后弹出操作菜单：

- 拍照
- 从相册选择
- 移除图片（仅在当前编辑态且已有图片时显示）

交互规则：

- 新建记录时，文字与当前已选图片一起保存
- 若只有图片没有文字，禁止保存并提示
- 已选图片在输入区上方显示一张小预览图
- 预览图右上角有关闭入口，可清除当前待保存图片

### Home record cards

- 记录卡片如果有 `photoPath`，在正文下方显示缩略图
- 点击缩略图打开大图预览
- 长按记录进入编辑时，支持查看当前缩略图并重新选择图片覆盖

### Review page

- 回顾页记录块复用同样的缩略图展示逻辑
- 点击缩略图打开大图预览
- 长按记录后，在编辑入口中支持重新拍照、重选图片和移除图片

### Large photo preview

- 使用单图大图预览层
- 点击关闭
- 本次不实现手势缩放、轮播和分享

## Backup Export and Import

### Export

备份导出时：

- 记录 JSON 不直接保留原本地绝对路径
- 先将 `photoPath` 转成 zip 内相对资源名
- 图片文件写入 zip，例如 `records/photos/<file>.jpg`

记录导出数据可采用：

- JSON 中记录逻辑字段
- 图片文件作为 zip 附件

这样导出的备份文件可以跨设备恢复，不依赖源设备绝对路径。

### Import

导入时：

- 先恢复图片资源到目标设备私有目录
- 为每张图生成新的本地正式路径
- 再把新的本地路径写回导入后的记录

异常规则：

- 某张图片缺失或损坏时，不阻断整包导入
- 对应记录允许作为“无图记录”继续导入
- 错误写入日志，并在导入结果中尽量保留文字记录

## S3 Sync

本次不新增单独的图片对象同步协议。

设计选择：

- 记录图片跟随现有备份 zip 同步到 S3
- 导出本地备份与上传 S3 使用同一套数据结构
- 下载 S3 备份并导入时，图片恢复逻辑与本地导入一致

理由：

- 与现有数据管理方式一致
- 冲突处理可直接复用
- 复杂度远低于对象级附件同步
- 当前需求只有单图记录，不值得引入独立媒体索引

## Conflict Resolution

当本地和导入源/同步源存在同一条记录且图片不同：

- `替换冲突项`：使用外部图片覆盖本地图，并删除本地旧图
- `合并`：保留本地图，忽略外部图片

该规则与现有记录、日记导入语义保持一致，不引入新的冲突选项。

## Error Handling

### Save-time rules

- 用户取消拍照或选图：静默返回
- 图像读取失败：不创建新记录，不修改旧记录
- 图像压缩失败：不覆盖旧图
- 有图但无文字：禁止保存并提示

### Edit-time rules

- 替换图片时必须先完成新图保存，再清理旧图
- 如果新图保存失败，旧图保持不变

### Delete-time rules

- 删除记录时同时删除图片文件
- 如果图片文件删除失败，记录仍删除，但写日志

### Render-time rules

- 缩略图加载失败时显示“图片不可用”占位
- 不影响用户查看这条文字记录

## Testing Strategy

### Data and storage tests

- `Record` / `RecordEntity` 新增 `photoPath` 的读写
- `RecordRepository` 更新记录图片路径
- `RecordPhotoStorage` 导入相册图、导入拍照图、压缩保存
- 新图覆盖旧图时旧文件被删除
- 删除记录时图片文件被删除

### UI state tests

- 首页有图/无图两种输入态
- 首页记录卡片有图/无图两种展示态
- 回顾页记录块有图/无图两种展示态
- 有图无文字时不能保存

### Backup and sync tests

- 导出 zip 时包含记录图片资源
- 导入 zip 时图片恢复成本地新路径
- `替换冲突项` 覆盖本地图
- `合并` 保留本地图

### Regression tests

- 无图记录继续按现有方式保存和编辑
- AI 日记生成逻辑不读取记录图片
- 现有备份、导入、S3 配置流程在无图场景下不回归

## File Impact

预计会修改或新增的主要文件：

- `app/src/main/java/com/aiawareness/diary/data/model/Record.kt`
- `app/src/main/java/com/aiawareness/diary/data/local/RecordEntity.kt`
- `app/src/main/java/com/aiawareness/diary/data/local/RecordDao.kt`
- `app/src/main/java/com/aiawareness/diary/data/local/DiaryDatabase.kt`
- `app/src/main/java/com/aiawareness/diary/data/repository/RecordRepository.kt`
- `app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt`
- `app/src/main/java/com/aiawareness/diary/data/backup/BackupModels.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/CalendarScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt`
- `app/src/main/java/com/aiawareness/diary/ui/components/...` 中承载缩略图的组件
- 新增 `app/src/main/java/com/aiawareness/diary/data/local/RecordPhotoStorage.kt`
- 新增拍照 `FileProvider` 配置及必要资源

## Open Decisions Resolved

- 每条记录最多一张图：已确定
- 编辑时再次上传图片：直接覆盖旧图：已确定
- 图片只在记录里展示，不进入 AI 日记：已确定
- 记录仍要求文字必填，不允许纯图片记录：已确定
- 图片保存时压缩后再入本地和同步：已确定
- 导入/同步图片冲突：跟随现有冲突策略：已确定

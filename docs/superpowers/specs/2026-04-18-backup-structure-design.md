# Backup Structure Design

## Goal

把导出/导入的备份结构整理成一套清晰、可回放、可验证的规范，确保记录、AI 日记、图片、头像和设置在一次导出后都能准确导入恢复。

## Current Problem

- 当前 zip 同时包含 `records/photos/...` 和 `journals/<date>/images/`，但后者只是空目录，结构语义不一致。
- 记录图片在导出时只保留文件名，存在同名冲突风险。
- 人工验证时不容易直观看出某一天的 markdown、图片和结构是否一致。
- 后续导入恢复依赖路径映射，但当前路径语义不够稳定。

## Confirmed Data Model

### Record

- `date`: `YYYY-MM-DD`
- `time`: `HH:mm`
- `content`
- `photoPath`
- `createdAt`
- `updatedAt`

### Diary

- `date`: `YYYY-MM-DD`
- `aiDiary`
- `aiInsight`
- `generatedAt`
- `updatedAt`

### UserSettings

- `nickname`
- `avatarPath`
- `profileQuote`
- `apiEndpoint`
- `apiKey`
- `modelName`
- `diaryGenerationHour`
- `diaryGenerationMinute`
- `s3Endpoint`
- `s3Bucket`
- `s3AccessKey`
- `s3SecretKey`
- `s3AutoSync`

## Target Zip Layout

```text
backup.zip
├── manifest.json
├── records.json
├── diaries.json
├── settings.json
└── assets/
    ├── avatar/
    │   └── avatar.jpg
    └── journals/
        ├── 2026-04-18/
        │   ├── entry.md
        │   └── images/
        │       ├── 001.jpg
        │       └── 002.jpg
        └── 2026-04-19/
            ├── entry.md
            └── images/
                └── 001.jpg
```

## Structure Rules

### Root JSON Files

- `manifest.json`: 描述备份版本、导出时间、记录数、AI 日记数、是否包含设置、是否包含头像。
- `records.json`: 机器导入的唯一记录数据源。
- `diaries.json`: 机器导入的唯一 AI 日记数据源。
- `settings.json`: 机器导入的唯一设置数据源。

### Assets Directory

- `assets/avatar/`: 头像资源目录。
- `assets/journals/<date>/entry.md`: 当天给人阅读的导出文档。
- `assets/journals/<date>/images/`: 当天记录引用的图片。

### Image Placement

- 不再保留全局 `records/photos/` 目录。
- 每天图片只保存在 `assets/journals/<date>/images/`。
- 图片命名使用日期内顺序编号：
  - `001.jpg`
  - `002.jpg`
  - `003.jpg`
- 图片在 zip 中只保存一份，不允许重复写入多个目录。

## Path Rules

### Record.photoPath in Exported JSON

导出后的 `records.json` 不再写应用私有绝对路径，而是写 zip 内相对路径，例如：

- `assets/journals/2026-04-18/images/001.jpg`

如果某条记录没有图片：

- `photoPath = ""`

### UserSettings.avatarPath in Exported JSON

导出后的 `settings.json` 中，头像路径写 zip 内相对路径，例如：

- `assets/avatar/avatar.jpg`

如果没有头像：

- `avatarPath = ""`

## Import Rules

- `records.json`、`diaries.json`、`settings.json` 是唯一事实来源。
- `entry.md` 只用于人工查看，不参与导入判定。
- 导入图片时，根据 `records.json` 里的相对路径从 zip 内定位图片，再恢复到应用私有目录，并把 `photoPath` 改写为新的本地绝对路径。
- 导入头像时，根据 `settings.json` 里的相对路径从 zip 内定位头像，再恢复到应用私有目录，并把 `avatarPath` 改写为新的本地绝对路径。

## Conflict Rules

- `Record` 冲突识别继续使用当前指纹：`date + time + content`。
- `Diary` 冲突识别继续使用 `date`。
- `Merge`:
  - 记录只导入不冲突项。
  - AI 日记只导入本地不存在日期的数据。
  - 设置仅补全本地空值或默认值。
- `ReplaceConflicts`:
  - 记录替换冲突项。
  - AI 日记替换同日期数据。
  - 设置整体采用导入值。

## Verification Principles

实现完成后，人工验证应该能直接通过解压 zip 完成：

1. 根目录 JSON 文件齐全。
2. `assets/journals/<date>/entry.md` 与当天图片目录同级存在。
3. `records.json` 内图片路径都能在 zip 中找到。
4. `settings.json` 的头像路径都能在 zip 中找到。
5. zip 中不存在重复图片目录语义，也不存在空 `images/` 目录但没有实际图片却被声称引用的情况。
6. 导入完成后，记录图片和头像都能在应用内正常显示。

## Out of Scope

- 本次规范不修改冲突判定逻辑本身。
- 本次规范不改变 AI 日记内容结构。
- 本次规范不改变 markdown 文案风格，只修正目录与资源引用关系。

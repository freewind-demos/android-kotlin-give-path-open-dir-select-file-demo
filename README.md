# android-kotlin-give-path-open-dir-select-file-demo

## 做什么

输一个绝对路径，点按钮。

app 会：

1. 解析目标路径
2. 推导父目录
3. 尽量拉起系统文件管理器落到该目录
4. 支持时尽量靠近目标文件

## 关键限制

Android 公共 API 不保证“按绝对路径打开文件管理器并高亮选中文件”。

也不保证：

1. 目标文件会自动滚到可视区域
2. 几百个文件的大目录里会自动定位到目标项
3. 不同 ROM / 文件管理器表现一致

根因：

1. 文件管理器实现各家不同
2. 系统没给稳定 public API 做“高亮某绝对路径文件”
3. 系统也没给稳定 public API 做“滚动到某绝对路径文件并让它进入可视区”
4. SAF 更偏“让用户选”，不是“程序按磁盘路径遥控文件管理器”
5. app 私有目录通常也不给外部文件管理器直接浏览

所以当前 demo 语义是：

1. 对共享存储路径构造 `DocumentsContract` Uri
2. 先试 `ACTION_VIEW` 打开目录
3. 不行再退到 `ACTION_OPEN_DOCUMENT_TREE + EXTRA_INITIAL_URI`
4. 最多做到“尽量打开到目标目录附近”，不是“稳定定位到目标文件”

## 支持路径

1. `/sdcard/...`
2. `/storage/emulated/0/...`
3. `/storage/self/primary/...`
4. `/storage/<volumeId>/...`

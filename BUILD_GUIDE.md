# 构建指南

## 问题解决

### Gradle版本问题
如果遇到Gradle版本错误，已经更新了以下文件：
- `gradle/wrapper/gradle-wrapper.properties` - 使用Gradle 8.0
- `build.gradle` - 使用Android Gradle Plugin 8.0.2
- `app/build.gradle` - 目标SDK降至33以确保兼容性

### 构建步骤

1. **在Android Studio中打开项目**
   ```
   File -> Open -> 选择 ArithScreenLock 文件夹
   ```

2. **同步项目**
   - Android Studio会自动提示同步
   - 或点击 "Sync Now"

3. **处理图标文件**
   - 当前图标文件为占位符文本文件
   - 需要替换为实际的PNG图标文件
   - 或者可以先删除这些占位符文件，Android Studio会使用默认图标

4. **删除占位符图标文件（推荐）**
   ```bash
   # 删除所有占位符图标文件
   find app/src/main/res/mipmap-* -name "*.png" -delete
   ```

5. **编译项目**
   ```bash
   ./gradlew assembleDebug
   ```

### 如果仍有问题

1. **清理项目**
   ```bash
   ./gradlew clean
   ```

2. **重新构建**
   ```bash
   ./gradlew build
   ```

3. **检查Java版本**
   - 确保使用Java 8或更高版本
   - Android Studio -> File -> Project Structure -> SDK Location

### 运行应用

1. 连接Android设备或启动模拟器
2. 点击Run按钮或使用命令：
   ```bash
   ./gradlew installDebug
   ```

## 注意事项

- 首次构建可能需要下载依赖，请确保网络连接正常
- 如果遇到权限问题，请确保gradlew文件有执行权限
- 建议使用Android Studio Arctic Fox或更新版本
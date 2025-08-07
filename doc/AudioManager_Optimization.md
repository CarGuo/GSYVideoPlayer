# AudioManager 内存泄漏优化方案

## 问题描述

在原始的 GSYVideoPlayer 实现中，AudioManager 的使用存在以下问题：

1. **内存泄漏风险**：`onAudioFocusChangeListener` 使用了匿名内部类，隐式持有外部类的引用，可能导致视频播放器实例无法被垃圾回收
2. **空指针异常**：某些代码路径中缺少对 AudioManager 的空值检查
3. **重复请求**：在继承层次结构中可能出现重复的音频焦点请求
4. **资源清理不完整**：在某些错误情况下，音频焦点可能没有被正确释放

## 解决方案

### 1. 创建 GSYAudioFocusManager 工具类

新的 `GSYAudioFocusManager` 类提供了以下特性：

#### 内存泄漏预防
- 使用 `WeakReference` 包装 AudioManager 和监听器
- 自动检测外部对象被回收的情况，并自动清理资源
- 主动释放资源的方法，避免长时间持有引用

#### 空值安全
- 所有 AudioManager 操作都经过空值检查
- 优雅处理初始化失败的情况
- 防御性编程模式，避免运行时崩溃

#### 重复请求防护
- 跟踪音频焦点状态，避免重复请求
- 集中管理音频焦点的生命周期
- 提供状态查询方法

#### 完善的错误处理
- 详细的日志记录，便于调试
- 异常捕获和处理
- 优雅的降级处理

### 2. 更新现有类

#### GSYVideoView.java
- 移除原有的匿名内部类监听器
- 实现 `GSYAudioFocusManager.GSYAudioFocusListener` 接口
- 使用新的音频焦点管理器替代直接的 AudioManager 操作
- 在适当的生命周期方法中释放资源

#### GSYVideoControlView.java
- 更新音量控制相关的代码，通过安全的方法获取 AudioManager
- 在 `onDetachedFromWindow` 中添加资源清理
- 所有 AudioManager 操作都经过空值检查

#### ListGSYVideoPlayer.java
- 更新音频焦点的释放逻辑

## 主要优化点

### 1. 内存管理
```java
// 旧方式：可能导致内存泄漏
protected AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = 
    new AudioManager.OnAudioFocusChangeListener() { ... };

// 新方式：使用弱引用避免内存泄漏
private final AudioManager.OnAudioFocusChangeListener mInternalListener = 
    new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            GSYAudioFocusListener listener = mListenerRef != null ? mListenerRef.get() : null;
            if (listener == null) {
                // 外部对象已被回收，自动清理
                abandonAudioFocusInternal();
                return;
            }
            // 处理音频焦点变化...
        }
    };
```

### 2. 空值安全
```java
// 旧方式：可能空指针异常
mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);

// 新方式：安全的空值检查
if (mAudioFocusManager != null) {
    mAudioFocusManager.abandonAudioFocus();
}
```

### 3. 资源管理
```java
// 新增的资源释放方法
@Override
protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    // ... 其他清理工作
    releaseAudioFocusManager(); // 确保资源被释放
}
```

## 使用示例

```java
// 在视频播放器中的使用
public class MyVideoPlayer extends GSYVideoView {
    
    @Override
    protected void init(Context context) {
        super.init(context);
        // AudioFocusManager 会自动初始化
    }
    
    // 音频焦点回调会自动处理
    @Override
    public void onAudioFocusLoss() {
        // 自定义失去音频焦点时的处理逻辑
        super.onAudioFocusLoss();
    }
    
    @Override
    protected void releaseVideos() {
        super.releaseVideos();
        // 资源会自动释放
    }
}
```

## 测试

提供了简单的测试类 `GSYAudioFocusManagerTest` 来验证基本功能：

```java
// 运行基本功能测试
GSYAudioFocusManagerTest.runAllTests();
```

## 性能影响

- **内存使用**：通过避免内存泄漏，长期内存使用更加稳定
- **CPU 使用**：增加了少量的空值检查，但影响微不足道
- **稳定性**：显著提高了应用的稳定性，减少了崩溃风险

## 向后兼容性

所有的更改都是内部实现的优化，对外部 API 没有破坏性更改：

- 现有的音频焦点回调方法仍然可用
- 音频焦点的行为逻辑保持不变
- 所有现有功能都得到保留

## 建议

1. **定期检查**：建议定期使用内存分析工具检查是否存在内存泄漏
2. **测试覆盖**：在不同 Android 版本和设备上测试音频焦点功能
3. **监控日志**：关注 GSYAudioFocusManager 的日志输出，及时发现潜在问题
4. **自定义扩展**：如需自定义音频焦点行为，可以重写相应的回调方法

## 总结

这次优化主要解决了 AudioManager 使用中的内存泄漏风险，提高了代码的健壮性和安全性。通过使用弱引用、集中管理和完善的错误处理，显著降低了因音频焦点管理不当导致的问题。
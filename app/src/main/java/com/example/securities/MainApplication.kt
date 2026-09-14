package com.example.securities

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 這是整個 App 的入口點與全域生命週期元件。
 */

@HiltAndroidApp // 關鍵：告訴 Hilt 生成全域依賴容器
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 可在此進行全域 SDK 或工具的初始化
    }
}
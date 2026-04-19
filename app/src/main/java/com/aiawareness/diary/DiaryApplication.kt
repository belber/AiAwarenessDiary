package com.aiawareness.diary

import android.app.Application
import android.content.pm.ApplicationInfo
import com.aiawareness.diary.data.local.UserPreferences
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.aliyun.emas.apm.Apm
import com.aliyun.emas.apm.ApmOptions
import com.aliyun.emas.apm.crash.ApmCrashAnalysisComponent
import com.aliyun.emas.apm.mem.monitor.ApmMemMonitorComponent
import com.aliyun.emas.apm.performance.ApmPerformanceComponent
import com.aliyun.emas.apm.remote.log.ApmRemoteLogComponent
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class DiaryApplication : Application(), Configuration.Provider {

    companion object {
        @Volatile
        private var apmStarted = false

        fun ensureApmStarted(): Boolean {
            if (apmStarted) {
                return true
            }
            synchronized(this) {
                if (!apmStarted) {
                    apmStarted = Apm.start()
                }
                return apmStarted
            }
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate() {
        super.onCreate()
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        Apm.preStart(
            ApmOptions.Builder()
                .setApplication(this)
                .setAppKey("335703758")
                .setAppSecret("21ad46bfe80b41ec9ee33df25ca8e0fc")
                .setAppRsaSecret("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCutP8ArIwqaHNRRnoCO7suhV1lzgQoFtShWu/UogpkLjoYXd9ihgckNTjsE+/+khImZXTy+cXEVzdqD4u8GX68lPdG9tSv3Cm/jXqGt5MlUvNmAtvHIZJOpQDL3O2pvtkimNsFc6blWOOZqn2ychtNhQt3Z6C8EkTKVjpNB2H8SQIDAQAB")
                .addComponent(ApmCrashAnalysisComponent::class.java)
                .addComponent(ApmMemMonitorComponent::class.java)
                .addComponent(ApmRemoteLogComponent::class.java)
                .addComponent(ApmPerformanceComponent::class.java)
                .openDebug(isDebuggable)
                .build()
        )

        runBlocking {
            if (userPreferences.isPrivacyPolicyAccepted.first()) {
                ensureApmStarted()
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

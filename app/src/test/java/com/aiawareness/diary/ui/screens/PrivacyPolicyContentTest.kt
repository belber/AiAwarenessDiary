package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyContentTest {

    @Test
    fun privacyPolicyFallback_mentionsCoreDataFlows() {
        val text = privacyPolicyFallbackText()

        assertTrue(text.contains("本地"))
        assertTrue(text.contains("OpenAI-compatible", ignoreCase = true))
        assertTrue(text.contains("S3-compatible", ignoreCase = true))
        assertTrue(text.contains("Aliyun APM", ignoreCase = true))
    }

    @Test
    fun privacyConsentDialogCopy_matchesStructuredSummary() {
        val resolver: (Int) -> String = { resId ->
            when (resId) {
                R.string.privacy_consent_title -> "隐私说明"
                R.string.privacy_consent_intro -> {
                    "继续使用前，请先确认数据如何保存、AI 如何调用，以及哪些运行信息会用于改进 APP。"
                }
                R.string.privacy_consent_section_local_heading -> "内容默认保存在本地"
                R.string.privacy_consent_section_local_body -> {
                    "你的日记、图片和设置默认只保存在本机。应用没有开发者自建服务端，不会收集或上传你的日记等用户内容。"
                }
                R.string.privacy_consent_section_ai_heading -> "AI 只使用你自己配置的服务"
                R.string.privacy_consent_section_ai_body -> {
                    "生成 AI 日记时，内容只会发送到你自己配置的 AI 大模型服务，不会提供给开发者或其他无关方。"
                }
                R.string.privacy_consent_section_runtime_heading -> "仅采集运行诊断信息"
                R.string.privacy_consent_section_runtime_body -> {
                    "在你同意后，应用才会启用阿里云移动监控等第三方能力，用于崩溃、性能、网络等运行情况分析，不用于收集你的日记内容。"
                }
                R.string.privacy_consent_policy_link -> "查看完整隐私政策"
                R.string.privacy_consent_agree_action -> "同意并继续"
                R.string.privacy_consent_disagree_action -> "不同意并退出"
                else -> error("Unexpected string resource id: $resId")
            }
        }

        assertEquals("隐私说明", privacyConsentDialogTitle(resolver))
        assertEquals(
            "继续使用前，请先确认数据如何保存、AI 如何调用，以及哪些运行信息会用于改进 APP。",
            privacyConsentDialogIntro(resolver)
        )
        assertEquals(
            listOf(
                PrivacyConsentSectionCopy(
                    heading = "内容默认保存在本地",
                    body = "你的日记、图片和设置默认只保存在本机。应用没有开发者自建服务端，不会收集或上传你的日记等用户内容。"
                ),
                PrivacyConsentSectionCopy(
                    heading = "AI 只使用你自己配置的服务",
                    body = "生成 AI 日记时，内容只会发送到你自己配置的 AI 大模型服务，不会提供给开发者或其他无关方。"
                ),
                PrivacyConsentSectionCopy(
                    heading = "仅采集运行诊断信息",
                    body = "在你同意后，应用才会启用阿里云移动监控等第三方能力，用于崩溃、性能、网络等运行情况分析，不用于收集你的日记内容。"
                )
            ),
            privacyConsentDialogSections(resolver)
        )
        assertEquals("查看完整隐私政策", privacyConsentDialogPolicyLinkLabel(resolver))
        assertEquals("同意并继续", privacyConsentDialogAgreeLabel(resolver))
        assertEquals("不同意并退出", privacyConsentDialogDisagreeLabel(resolver))
    }
}

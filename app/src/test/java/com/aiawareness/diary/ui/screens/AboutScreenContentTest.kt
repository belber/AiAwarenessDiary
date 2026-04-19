package com.aiawareness.diary.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class AboutScreenContentTest {

    @Test
    fun aboutPageContent_matchesPrototypeCopy() {
        assertEquals(
            AboutScreenContent(
                bannerCaption = "致每一位寻求宁静的旅者",
                title = "呼吸之间，自见本心",
                philosophyParagraphs = listOf(
                    "我们身处一个喧嚣的时代，信息如潮水般涌入，却鲜有片刻留给内心的呼吸。AI Awareness Journal 的诞生，并非为了增加你的数字负担，而是希望在数字荒原中为你开辟一处“数字避风港”（The Digital Sanctuary）。",
                    "我们推崇 Yutori —— 这是一种关于“余白”的智慧。在这里，留白不仅仅是视觉上的空隙，更是心灵得以喘息的空间。我们不追求繁杂的数据分析，只愿通过最柔和的笔触，协助你捕捉那一抹转瞬即逝的觉察。",
                    "从呼吸到自我（From breath to self），我们相信记录的力量不在于长度，而在于那一刻的专注与诚实。"
                ),
                ownershipHighlight = "你的内容不应被锁在某个产品里。我们支持将日记按每天导出为可直接阅读的 Markdown 文件，并保留可恢复的备份结构，让你随时带走、归档、迁移。",
                privacyEyebrow = "隐私承诺 / DATA PRIVACY",
                privacyIntro = "你的思考应当是私密的，如同锁在抽屉里的日记本。我们深刻理解数据主权的重要性，因此在设计之初就确立了最严格的隐私边界：",
                privacyBullets = listOf(
                    "所有的文字与情感数据默认仅存储于你的本地设备。我们不设中心化服务器，不收集任何形式的用户日记内容。",
                    "只有在你主动配置第三方 AI 或对象存储服务时，相关内容才会由客户端直接发送到你指定的服务提供商。",
                    "在你同意隐私政策后，应用会启用 Aliyun APM 用于崩溃、性能、网络、日志与内存诊断；我们也建议你审慎选择第三方服务并保管好自己的接口密钥。"
                ),
                privacyFooter = "在这里，你的文字只属于你自己，现在如此，未来亦然。",
                footerBlessing = "愿你在每一次记录中，都能遇见更轻盈的自己。",
                versionLabel = "Version 1.0.4 • The Breath of Silence Edition"
            ),
            aboutScreenContent()
        )
    }
}

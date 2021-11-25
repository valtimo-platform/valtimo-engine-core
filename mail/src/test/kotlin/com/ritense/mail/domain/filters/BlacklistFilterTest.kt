package com.ritense.mail.domain.filters

import com.ritense.mail.BaseTest
import com.ritense.mail.config.MailingProperties
import com.ritense.mail.service.BlacklistService
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

internal class BlacklistFilterTest : BaseTest() {

    lateinit var testRecipient: Recipient
    lateinit var blacklistedRecipient: Recipient
    lateinit var blacklistService: BlacklistService

    @BeforeEach
    internal fun setUp() {
        blacklistService = mock(BlacklistService::class.java)
        testRecipient = Recipient.to(EmailAddress.from("test@ritense.com"), SimpleName.from("test"))
        blacklistedRecipient = Recipient.to(EmailAddress.from("blacklisted@ritense.com"), SimpleName.from("Blacklist"))
    }

    @Test
    fun shouldFilterOutBlacklistedRecipient() {
        `when`(blacklistService.isBlacklisted(testRecipient.email.get())).thenReturn(true)
        val blacklistFilter = BlacklistFilter(MailingProperties(), blacklistService)
        val rawMailMessageTest: RawMailMessage = rawMailMessage(testRecipient)
        blacklistFilter.apply(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isFalse
        assertThat(rawMailMessageTest.recipients.get()).isEmpty()
    }

    @Test
    fun shouldNotFilterOutRecipient() {
        `when`(blacklistService.isBlacklisted(testRecipient.email.get())).thenReturn(false)
        val blacklistFilter = BlacklistFilter(MailingProperties(), blacklistService)
        val rawMailMessageTest: RawMailMessage = rawMailMessage(blacklistedRecipient)
        blacklistFilter.apply(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isTrue
        assertThat(rawMailMessageTest.recipients.get()).containsOnly(blacklistedRecipient)
    }

    @Test
    fun filterShouldDefaultBeEnabled() {
        val blacklistFilter = BlacklistFilter(MailingProperties(), blacklistService)
        assertThat(blacklistFilter.isEnabled).isTrue
    }

    @Test
    fun filterShouldBeDisabledWhenIsBlacklistFilterIsFalse() {
        val blacklistFilter = BlacklistFilter(MailingProperties(isBlacklistFilter = false), blacklistService)
        assertThat(blacklistFilter.isEnabled).isFalse
    }

    @Test
    fun filterPriorityShouldDefaultMinus1() {
        val blacklistFilter = BlacklistFilter(MailingProperties(), blacklistService)
        assertThat(blacklistFilter.priority).isEqualTo(10)
    }

    @Test
    fun filterPriorityShouldBe1() {
        val blacklistFilter = BlacklistFilter(MailingProperties(blacklistFilterPriority = 1), blacklistService)
        assertThat(blacklistFilter.priority).isEqualTo(1)
    }

}
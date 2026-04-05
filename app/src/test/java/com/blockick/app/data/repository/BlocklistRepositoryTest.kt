package com.blockick.app.data.repository

import com.blockick.app.data.db.dao.BlocklistDao
import com.blockick.app.data.db.dao.CustomListDao
import com.blockick.app.data.db.dao.DomainDao
import com.blockick.app.data.db.dao.UserBlocklistDao
import com.blockick.app.data.db.entities.BlocklistEntity
import com.blockick.app.data.parsers.AdblockParser
import com.blockick.app.data.parsers.DomainListParser
import com.blockick.app.data.parsers.HostsFileParser
import com.blockick.app.data.parsers.JsonParser
import com.blockick.app.domain.engine.BlocklistEngine
import com.blockick.app.util.NotificationHelper
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlocklistRepositoryTest {

    private lateinit var repository: BlocklistRepository
    private val blocklistDao = mockk<BlocklistDao>(relaxed = true)
    private val customListDao = mockk<CustomListDao>(relaxed = true)
    private val domainDao = mockk<DomainDao>(relaxed = true)
    private val userBlocklistDao = mockk<UserBlocklistDao>(relaxed = true)
    private val hostsParser = mockk<HostsFileParser>(relaxed = true)
    private val domainParser = mockk<DomainListParser>(relaxed = true)
    private val adblockParser = mockk<AdblockParser>(relaxed = true)
    private val jsonParser = mockk<JsonParser>(relaxed = true)
    private val okHttpClient = mockk<OkHttpClient>(relaxed = true)
    private val blocklistEngine = mockk<BlocklistEngine>(relaxed = true)
    private val notificationHelper = mockk<NotificationHelper>(relaxed = true)

    private val allLists = listOf(
        BlocklistEntity(id = "oisd", name = "OISD", url = "", format = "domain"),
        BlocklistEntity(id = "oisd-big", name = "Big", url = "https://big.oisd.nl", format = "domain", parentId = "oisd"),
        BlocklistEntity(id = "oisd-small", name = "Small", url = "https://small.oisd.nl", format = "domain", parentId = "oisd"),
        BlocklistEntity(id = "stevenblack", name = "Steven Black", url = "", format = "hosts"),
        BlocklistEntity(id = "stevenblack-unified", name = "Unified", url = "https://raw.../hosts", format = "hosts", parentId = "stevenblack"),
        BlocklistEntity(id = "adaway", name = "AdAway", url = "", format = "hosts"),
        BlocklistEntity(id = "adaway-main", name = "AdAway", url = "https://raw.../hosts.txt", format = "hosts", parentId = "adaway"),
        BlocklistEntity(id = "1hosts", name = "1Hosts", url = "", format = "hosts"),
        BlocklistEntity(id = "1hosts-lite", name = "Lite", url = "https://raw.../lite/hosts.txt", format = "hosts", parentId = "1hosts"),
        BlocklistEntity(id = "1hosts-pro", name = "Pro", url = "https://raw.../pro/hosts.txt", format = "hosts", parentId = "1hosts"),
        BlocklistEntity(id = "goodbyeads", name = "Goodbye Ads", url = "", format = "hosts"),
        BlocklistEntity(id = "goodbyeads-extended", name = "Extended", url = "https://raw.../extended/hosts.txt", format = "hosts", parentId = "goodbyeads"),
        BlocklistEntity(id = "energized", name = "Hagezi", url = "", format = "hosts"),
        BlocklistEntity(id = "energized-spark", name = "Light", url = "https://raw.../light.txt", format = "hosts", parentId = "energized"),
        BlocklistEntity(id = "energized-blu", name = "Normal", url = "https://raw.../multi.txt", format = "hosts", parentId = "energized"),
        BlocklistEntity(id = "energized-ultimate", name = "Pro", url = "https://raw.../pro.txt", format = "hosts", parentId = "energized")
    )

    @Before
    fun setUp() {
        coEvery { blocklistDao.getAll() } returns flowOf(allLists)
        repository = BlocklistRepository(
            blocklistDao, customListDao, domainDao, userBlocklistDao,
            hostsParser, domainParser, adblockParser, jsonParser,
            okHttpClient, blocklistEngine, notificationHelper
        )
    }

    @Test
    fun `applyProfile Ultra enables correct lists`() = runBlocking {
        val capturedLists = slot<List<BlocklistEntity>>()
        coEvery { blocklistDao.updateAll(capture(capturedLists)) } returns Unit

        repository.applyProfile("Ultra")

        val enabledIds = capturedLists.captured.filter { it.isEnabled }.map { it.id }
        
        assertTrue(enabledIds.contains("1hosts-pro"))
        assertTrue(enabledIds.contains("goodbyeads-extended"))
        assertTrue(enabledIds.contains("energized-ultimate"))
        assertTrue(enabledIds.contains("oisd-big"))
        assertTrue(enabledIds.contains("stevenblack-unified"))
        assertTrue(!enabledIds.contains("1hosts-lite"))
        assertTrue(!enabledIds.contains("energized-blu"))
    }

    @Test
    fun `applyProfile Balanced enables correct lists`() = runBlocking {
        val capturedLists = slot<List<BlocklistEntity>>()
        coEvery { blocklistDao.updateAll(capture(capturedLists)) } returns Unit

        repository.applyProfile("Balanced")

        val enabledIds = capturedLists.captured.filter { it.isEnabled }.map { it.id }
        
        assertTrue(enabledIds.contains("oisd-big"))
        assertTrue(enabledIds.contains("1hosts-lite"))
        assertTrue(enabledIds.contains("energized-blu"))
        assertTrue(!enabledIds.contains("1hosts-pro"))
        assertTrue(!enabledIds.contains("energized-ultimate"))
    }

    @Test
    fun `applyProfile Minimal enables correct lists`() = runBlocking {
        val capturedLists = slot<List<BlocklistEntity>>()
        coEvery { blocklistDao.updateAll(capture(capturedLists)) } returns Unit

        repository.applyProfile("Minimal")

        val enabledIds = capturedLists.captured.filter { it.isEnabled }.map { it.id }
        
        assertTrue(enabledIds.contains("oisd-small"))
        assertTrue(enabledIds.contains("energized-spark"))
        assertTrue(!enabledIds.contains("oisd-big"))
        assertTrue(!enabledIds.contains("energized-blu"))
    }
}

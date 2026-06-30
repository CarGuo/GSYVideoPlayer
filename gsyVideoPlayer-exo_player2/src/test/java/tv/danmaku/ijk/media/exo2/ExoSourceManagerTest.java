package tv.danmaku.ijk.media.exo2;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

public class ExoSourceManagerTest {

    @After
    public void tearDown() {
        ExoSourceManager.resetCacheMaxSize();
    }

    @Test
    public void cacheMaxSizeUsesDefaultValue() {
        Assert.assertEquals(ExoSourceManager.DEFAULT_CACHE_MAX_SIZE, ExoSourceManager.getCacheMaxSize());
    }

    @Test
    public void cacheMaxSizeCanBeConfiguredBeforeCacheCreation() {
        long customMaxSize = 1024L * 1024L * 1024L;

        ExoSourceManager.setCacheMaxSize(customMaxSize);

        Assert.assertEquals(customMaxSize, ExoSourceManager.getCacheMaxSize());
    }

    @Test
    public void cacheMaxSizeCanBeResetToDefaultValue() {
        ExoSourceManager.setCacheMaxSize(128L * 1024L * 1024L);

        ExoSourceManager.resetCacheMaxSize();

        Assert.assertEquals(ExoSourceManager.DEFAULT_CACHE_MAX_SIZE, ExoSourceManager.getCacheMaxSize());
    }

    @Test
    public void cacheMaxSizeRejectsInvalidValue() {
        try {
            ExoSourceManager.setCacheMaxSize(0);
            Assert.fail("Expected invalid cache max size to throw");
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("cacheMaxSize must be greater than 0", expected.getMessage());
        }
    }

    @Test
    public void cacheMaxSizeCannotChangeAfterCacheCreation() throws Exception {
        Map<String, Object> cacheHolderMap = getCacheHolderMap();
        cacheHolderMap.put("active-cache", null);

        try {
            ExoSourceManager.setCacheMaxSize(256L * 1024L * 1024L);
            Assert.fail("Expected active cache holder to reject max size changes");
        } catch (IllegalStateException expected) {
            Assert.assertEquals("Exo cache max size must be set before cache is created", expected.getMessage());
        } finally {
            cacheHolderMap.clear();
        }
    }

    @Test
    public void cacheMaxSizeCannotResetAfterCacheCreation() throws Exception {
        Map<String, Object> cacheHolderMap = getCacheHolderMap();
        cacheHolderMap.put("active-cache", null);

        try {
            ExoSourceManager.resetCacheMaxSize();
            Assert.fail("Expected active cache holder to reject max size reset");
        } catch (IllegalStateException expected) {
            Assert.assertEquals("Exo cache max size must be reset before cache is created", expected.getMessage());
        } finally {
            cacheHolderMap.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCacheHolderMap() throws Exception {
        Field cacheHolderMap = ExoSourceManager.class.getDeclaredField("sCacheHolderMap");
        cacheHolderMap.setAccessible(true);
        return (Map<String, Object>) cacheHolderMap.get(null);
    }
}

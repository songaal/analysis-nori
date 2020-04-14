package com.danawa.search.analysis.plugin;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.danawa.search.analysis.dict.SimpleDictionary;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryTest {

    private static Logger logger = LoggerFactory.getLogger(DictionaryTest.class);

    @Before
    public void init() {
        logger.debug("init");
    }

    @Test public void simpleDictionaryTest() throws Exception {
        logger.debug("START.");
        List<String> entries = new ArrayList<>();
        entries.addAll(Arrays.asList("세종,대왕,대한,민국".split("[,]")));
        SimpleDictionary dict = new SimpleDictionary(entries);
        assertTrue(dict.contains("대왕"));
        assertTrue(dict.contains("세종"));
        assertTrue(dict.contains("민국"));
        assertTrue(!dict.contains("미국"));
    }

    @Test
    public void preAnalysisDictionaryTest() throws Exception {
        assertTrue(true);
    }
}
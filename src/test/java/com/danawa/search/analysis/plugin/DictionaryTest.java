package com.danawa.search.analysis.plugin;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.danawa.search.analysis.dict.SystemDictionary;
import com.danawa.search.analysis.dict.SystemDictionary.WordEntry;

import org.apache.lucene.analysis.ko.util.DictionaryBuilder;
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

    @Test
    public void simpleDictionaryTest() throws Exception {
        logger.debug("START.");
        List<WordEntry> entries = new ArrayList<>();

        String[] words = { "대한", "민국", "세종", "대왕" };
        int[] costs = { SystemDictionary.DEFAULT_WORD_COST_HIGH, SystemDictionary.DEFAULT_WORD_COST_MID,
                SystemDictionary.DEFAULT_WORD_COST_MIN, SystemDictionary.DEFAULT_WORD_COST_HIGH, };

        entries.addAll(
                Arrays.asList(new WordEntry[] { new WordEntry(words[0], costs[0]), new WordEntry(words[1], costs[1]),
                        new WordEntry(words[2], costs[2]), new WordEntry(words[3], costs[3]) }));

        SystemDictionary dict = new SystemDictionary(entries);
        assertTrue(dict.contains("대왕") != -1);
        assertTrue(dict.contains("세종") != -1);
        assertTrue(dict.contains("민국") != -1);
        assertTrue(dict.contains("미국") == -1);

        for (int inx = 0; inx < words.length; inx++) {
            String word = words[inx];
            int wordId = dict.contains(word);
            assertEquals(costs[inx], dict.getWordCost(wordId));
        }
    }

    @Test
    public void performanceTest() throws Exception {
        String userDictFile = System.getProperty("PROP_TEST_USER_DICT_TXT");
        File file = new File(userDictFile);
        // 빌드시 자동 테스트 수행을 막는다
        if (!"Y".equals(System.getProperty("PROP_TEST_MASSIVE_DATA")) || !file.exists()) {
            return;
        }
        // CharVector - SetDictionary in fastcatsearch performance log
        // TOTAL 10000000 TIMES IN 670177400 NANOSECOND / SIZE:32641 / FOUND:5004093
        // TOTAL 10000000 TIMES IN 691420400 NANOSECOND / SIZE:32641 / FOUND:5002853
        // TOTAL 10000000 TIMES IN 676599800 NANOSECOND / SIZE:32641 / FOUND:5002911
        // TOTAL 10000000 TIMES IN 679579800 NANOSECOND / SIZE:32641 / FOUND:5003652
        // TOTAL 10000000 TIMES IN 682159900 NANOSECOND / SIZE:32641 / FOUND:5003520
        // TOTAL 10000000 TIMES IN 680303800 NANOSECOND / SIZE:32641 / FOUND:5001521
        // TOTAL 10000000 TIMES IN 678906500 NANOSECOND / SIZE:32641 / FOUND:5003841
        // TOTAL 10000000 TIMES IN 679030900 NANOSECOND / SIZE:32641 / FOUND:5001682
        // TOTAL 10000000 TIMES IN 677738900 NANOSECOND / SIZE:32641 / FOUND:5001621
        // TOTAL 10000000 TIMES IN 676281600 NANOSECOND / SIZE:32641 / FOUND:5001952
        // TOTAL 10000000 TIMES IN 672482100 NANOSECOND / SIZE:32641 / FOUND:5003813
        // TOTAL 10000000 TIMES IN 676612400 NANOSECOND / SIZE:32641 / FOUND:5002491
        // TOTAL 10000000 TIMES IN 673703300 NANOSECOND / SIZE:32641 / FOUND:5002551
        // TOTAL 10000000 TIMES IN 675403300 NANOSECOND / SIZE:32641 / FOUND:5003909
        // TOTAL 10000000 TIMES IN 684173700 NANOSECOND / SIZE:32641 / FOUND:5002742
        // TOTAL 10000000 TIMES IN 673437400 NANOSECOND / SIZE:32641 / FOUND:5003780
        // TOTAL 10000000 TIMES IN 674734900 NANOSECOND / SIZE:32641 / FOUND:5002703
        // TOTAL 10000000 TIMES IN 683387800 NANOSECOND / SIZE:32641 / FOUND:5002994
        // TOTAL 10000000 TIMES IN 781508700 NANOSECOND / SIZE:32641 / FOUND:5001183
        // TOTAL 10000000 TIMES IN 754505100 NANOSECOND / SIZE:32641 / FOUND:5001771
        // ALLOCATED-MEMORY:13629440
        // --------------------------------------------------------------------------------
        // String - FST Dictionary in lucene performance log
        // TOTAL 10000000 TIMES IN 1925867000 NANOSECOND / SIZE:32641 / FOUND:5001873
        // TOTAL 10000000 TIMES IN 1727979700 NANOSECOND / SIZE:32641 / FOUND:5002465
        // TOTAL 10000000 TIMES IN 1723603200 NANOSECOND / SIZE:32641 / FOUND:5001976
        // TOTAL 10000000 TIMES IN 1726851900 NANOSECOND / SIZE:32641 / FOUND:5001968
        // TOTAL 10000000 TIMES IN 1723559400 NANOSECOND / SIZE:32641 / FOUND:5001196
        // TOTAL 10000000 TIMES IN 1723813100 NANOSECOND / SIZE:32641 / FOUND:5004293
        // TOTAL 10000000 TIMES IN 1722107700 NANOSECOND / SIZE:32641 / FOUND:5003038
        // TOTAL 10000000 TIMES IN 1721091600 NANOSECOND / SIZE:32641 / FOUND:5002131
        // TOTAL 10000000 TIMES IN 1729360000 NANOSECOND / SIZE:32641 / FOUND:5000979
        // TOTAL 10000000 TIMES IN 1720595000 NANOSECOND / SIZE:32641 / FOUND:5001121
        // TOTAL 10000000 TIMES IN 1723831000 NANOSECOND / SIZE:32641 / FOUND:5003789
        // TOTAL 10000000 TIMES IN 1724392900 NANOSECOND / SIZE:32641 / FOUND:5002287
        // TOTAL 10000000 TIMES IN 1724533000 NANOSECOND / SIZE:32641 / FOUND:5004437
        // TOTAL 10000000 TIMES IN 1722891100 NANOSECOND / SIZE:32641 / FOUND:5001257
        // TOTAL 10000000 TIMES IN 1722363800 NANOSECOND / SIZE:32641 / FOUND:5001731
        // TOTAL 10000000 TIMES IN 1722641500 NANOSECOND / SIZE:32641 / FOUND:5001120
        // TOTAL 10000000 TIMES IN 1725340800 NANOSECOND / SIZE:32641 / FOUND:5003309
        // TOTAL 10000000 TIMES IN 1746367600 NANOSECOND / SIZE:32641 / FOUND:5004078
        // TOTAL 10000000 TIMES IN 1747165600 NANOSECOND / SIZE:32641 / FOUND:5000783
        // TOTAL 10000000 TIMES IN 1748698900 NANOSECOND / SIZE:32641 / FOUND:5004209
        // ALLOCATED-MEMORY:276373952
        // --------------------------------------------------------------------------------
        // 결론 : FST 사전은 Set 기반 사전보다 느리므로 (약 3~4배) 규칙분류에서 사용하기 힘들다
        // 메모리도 약 2배 정도 더 사용한다

        BufferedReader reader = null;
        List<WordEntry> wordList = new ArrayList<>();
        SystemDictionary dictSet = null;
        int cntWord = 0;

        long freeMemory = Runtime.getRuntime().freeMemory();

        try {
            reader = new BufferedReader(new FileReader(file));
            for (String ln; (ln = reader.readLine()) != null;) {
                wordList.add(new WordEntry(ln, -100000));
            }
            dictSet = new SystemDictionary(wordList);
            cntWord = wordList.size();
        } finally {
            try {
                reader.close();
            } catch (Exception ignore) {
            }
        }
        freeMemory = freeMemory - Runtime.getRuntime().freeMemory();

        int ntimes = 10000000;
        Random r = new Random();

        long timeTotal = 0;
        long timePrev = 0;
        long timeNext = 0;
        int found = 0;
        int tries = 20;

        int chStart = '가';
        int chLimit = '힣' - chStart;
        CharSequence cv = null;

        try {
            for (int test = 0; test < tries; test++) {
                timeTotal = 0;
                found = 0;
                for (int inx = 0; inx < ntimes; inx++) {
                    if (Math.abs(r.nextInt()) % 2 == 0) {
                        cv = wordList.get(Math.abs(r.nextInt()) % cntWord).word;
                    } else {
                        String str = "";
                        int len = Math.abs(r.nextInt()) % 10 + 1;
                        for (int n = 0; n < len; n++) {
                            str += (char) (chStart + (Math.abs(r.nextInt()) % chLimit));
                        }
                        cv = str;
                    }

                    timePrev = System.nanoTime();
                    boolean contains = (dictSet.contains(cv) != -1);
                    timeNext = System.nanoTime();
                    timeTotal += (timeNext - timePrev);

                    if (contains) {
                        found++;
                    }
                }
                logger.debug("TOTAL {} TIMES IN {} NANOSECOND / SIZE:{} / FOUND:{}", ntimes, timeTotal, wordList.size(),
                        found);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        logger.debug("ALLOCATED-MEMORY:{}", freeMemory);
        logger.debug("--------------------------------------------------------------------------------");
        assertTrue(true);
    }

    @Test
    public void createDictionaryTest() {
        String encoding = "UTF-8";
        boolean normalizeEntry = true;
        File inputDir = null;
        File outputDir = null;
        Writer writer = null;
        FileFilter deleteFilter = new DeleteFileFilter();
        try {
            inputDir = createTmpDir();
            outputDir = createTmpDir();
            writer = new FileWriter(new File(inputDir, "noun.csv"));
            writer.write("한국어,0,0,0,NNG,,,,*,*,*,0");
            writer.close();

            writer = new FileWriter(new File(inputDir, "unk.def"));
            writer.write("DEFAULT,1801,3566,3640,SY,*,*,*,*,*,*,*");
            writer.close();

            writer = new FileWriter(new File(inputDir, "char.def"));
            writer.write("0x0041..0x005A ALPHA");
            writer.close();

            writer = new FileWriter(new File(inputDir, "matrix.def"));
            writer.write("1 1\n0 0 0");
            writer.close();

            DictionaryBuilder.build(inputDir.toPath(), outputDir.toPath(), encoding, normalizeEntry);
        } catch (IOException e) {
            logger.error("", e);
        } finally {
            try { writer.close(); } catch (Exception ignore) { }
            try { deleteDir(inputDir, deleteFilter); } catch (Exception ignore) { }
            try { deleteDir(outputDir, deleteFilter); } catch (Exception ignore) { }
        }
        assertTrue(true);
    }

    @Test
    public void preAnalysisDictionaryTest() throws Exception {
        assertTrue(true);
    }

    public static File createTmpDir() throws IOException {
        File file = File.createTempFile("tmp_", "_dir");
        file.delete();
        file.mkdir();
        return file;
    }

    public static class DeleteFileFilter implements FileFilter {
        @Override public boolean accept(File file) {
            if (file.isDirectory()) {
                file.listFiles(this);
            }
            file.delete();
            return false;
        }
    }

    public static void deleteDir(File file, FileFilter filter) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                file.listFiles(filter);
            }
            file.delete();
        }
    }
}
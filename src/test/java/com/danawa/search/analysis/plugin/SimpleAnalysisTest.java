package com.danawa.search.analysis.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.danawa.search.analysis.dict.CommonDictionary;
import com.danawa.search.analysis.dict.SystemDictionary;
import com.danawa.search.analysis.index.ProductNameTokenizerFactory;
import com.danawa.search.analysis.productname.ProductNameAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.Token;
import org.apache.lucene.analysis.ko.KoreanTokenizer.DecompoundMode;
import org.apache.lucene.analysis.ko.POS.Tag;
import org.apache.lucene.analysis.ko.dict.Dictionary;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.apache.lucene.analysis.ko.tokenattributes.TokenAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

public class SimpleAnalysisTest {
    private static Logger logger = LoggerFactory.getLogger(SimpleAnalysisTest.class);
    private static final String TEXT_STR = "아름다운이땅에금수강산에단군할아버지가터잡으시고홍익인간뜻으로나라세우니대대손손훌륭한인물도많아고구려세운동명왕백제온조왕알에서나온혁거세만주벌판달려라광개토대왕신라장군이사부백결선생떡방아삼천궁녀의자왕황산벌의계백맞서싸운관창역사는흐른다말목자른김유신통일문무왕원효대사해골물혜초천축국바다의왕자장보고발해대조영귀주대첩강감찬서희거란족무단정치정중부화포최무선죽림칠현김부식지눌국사조계종의천천태종대마도정벌이종무일편단심정몽주목화씨는문익점해동공자최충삼국유사일연역사는흐른다황금을보기를돌같이하라최영장군의말씀받들자황희정승맹사성과학장영실신숙주와한명회역사는안다십만양병이율곡주리이퇴계신사임당오죽헌잘싸운다곽재우조헌김시민나라구한이순신태정태세문단세사육신과생육신몸바쳐서논개행주치마권율역사는흐른다번쩍번쩍홍길동의적임꺽정대쪽같은삼학사어사박문수삼년공부한석봉단원풍속도방랑시인김삿갓지도김정호영조대왕신문고정조규장각목민심서정약용녹두장군전봉준순교김대건서화가무황진이못살겠다홍경래삼일천하김옥균안중근은애국이완용은매국역사는흐른다별헤는밤윤동주종두지석영삼십삼인손병희만세만세유관순도산안창호어린이날방정환이수일과심순애장군의아들김두한날자꾸나이상황소그림중섭역사는흐른다";
    private static final String USER_DICT_STR = "금수강산,홍익인간,동명왕,온조왕,말목,김유신,통일,문무왕,12ab".replaceAll("[, ]", "\n");
    private static final String STOP_TAG_STR = "SP";//"NR,SP";

    @Test
    public void simpleTokenizerTest() throws Exception {
        UserDictionary userDictionary = null;
        DecompoundMode decompoundMode = DecompoundMode.MIXED;
        boolean discardPunctuation = false;
        Tokenizer stream = null;
        {
            Reader reader = new StringReader(USER_DICT_STR);
            userDictionary = UserDictionary.open(reader);
        }
        try {
            String text = TEXT_STR;
            ////////////////////////////////////////////////////////////////////////////////
            //decompoundMode = DecompoundMode.DISCARD;
            //text = "상품명분석기1234abcd테스트중입니다";
            ////////////////////////////////////////////////////////////////////////////////
            stream = createTokenizer(userDictionary, decompoundMode, discardPunctuation);
            Reader input = new StringReader(text);
            stream.setReader(input);
            stream.reset();
            int pos = 0;
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            PositionIncrementAttribute pincAtt = stream.addAttribute(PositionIncrementAttribute.class);
            OffsetAttribute offsAtt = stream.addAttribute(OffsetAttribute.class);
            while (stream.incrementToken()) {
                logger.debug("TERM:{} / {}~{} / {}", termAtt, offsAtt.startOffset(),
                        offsAtt.endOffset(), pos += pincAtt.getPositionIncrement());
            }
        } finally {
            stream.close();
        }
        assertTrue(true);
    }

    @Test
    public void simpleAnalyzerTest() throws Exception {
        SystemDictionary userDictionary = null;
        DecompoundMode decompoundMode = DecompoundMode.MIXED;
        Set<Tag> stopTags = null;
        {
            Reader reader = new StringReader(USER_DICT_STR);
            userDictionary = SystemDictionary.open(reader);
        }
        {
            stopTags = new HashSet<Tag>();
            for (String tagStr : STOP_TAG_STR.split("[,]")) {
                stopTags.add(POS.resolveTag(tagStr));
            }
            if (stopTags.size() == 0) {
                stopTags = null;
            }
        }
        Analyzer analyzer = null;
        TokenStream stream = null;
        try {
            String text = TEXT_STR;
            ////////////////////////////////////////////////////////////////////////////////
            //decompoundMode = DecompoundMode.NONE;
            //text = "상품명분석기1234abcd12ab34cd테스트중입니다";
            ////////////////////////////////////////////////////////////////////////////////
            Reader input = new StringReader(text);
            analyzer = new ProductNameAnalyzer(userDictionary, decompoundMode, stopTags, false, false);
            stream = analyzer.tokenStream("", input);
            stream.reset();
            stream.clearAttributes();
            int pos = 0;
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            PositionIncrementAttribute pincAtt = stream.addAttribute(PositionIncrementAttribute.class);
            OffsetAttribute offsAtt = stream.addAttribute(OffsetAttribute.class);
            TokenAttribute tokenAtt = stream.addAttribute(TokenAttribute.class);

            Object[] types = { 
                POS.Tag.NNB, POS.Tag.NNBC, POS.Tag.NNG, POS.Tag.NNP,
                POS.Tag.VA, POS.Tag.VV, POS.Tag.VX,
                POS.Tag.SN, POS.Tag.SL
            };

            while (stream.incrementToken()) {
                Object type = null;
                Token token = tokenAtt.getToken();
                type = token.getLeftPOS();

                boolean visible = false;

                if (isIn(type, types)) {
                    visible = true;
                }

                if (visible) {
                    logger.debug("TERM:{} / {} / {}~{} / {}", termAtt, type, offsAtt.startOffset(),
                            offsAtt.endOffset(), pos += pincAtt.getPositionIncrement());
                } else {
                    logger.debug("TERM:{} / {}", termAtt, type);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            stream.close();
            analyzer.close();
        }
        assertTrue(true);
    }

    public boolean isIn(Object obj, Object[] arr) {
        boolean ret = false;
        for (Object value : arr) {
            if (obj != null && obj.equals(value)) {
                return true;
            }
        }
        return ret;
    }

    @Test
    public void testDictionaryLoadedTokenizer() throws Exception {
        Properties prop = new Properties(0);
        File propFile = null;
        try { propFile = new File(System.getProperty("PROP_TEST_DICTIONARY_SETTING")); } catch (Exception ignore) { }
        // 빌드시 자동 테스트 수행을 막는다
        if (!"Y".equals(System.getProperty("PROP_TEST_MASSIVE_DATA")) || !(propFile != null && propFile.exists())) {
            return;
        }
        {
            Reader reader = null;
            try {
                reader = new FileReader(propFile);
                prop.load(reader);
            } finally {
                try { reader.close(); } catch (Exception ignore) { }
            }
        }
        CommonDictionary commonDictionary = ProductNameTokenizerFactory.loadDictionary(null, prop);
        SystemDictionary userDictionary = commonDictionary.getSystemDictionary();
        DecompoundMode decompoundMode = DecompoundMode.MIXED;
        Set<Tag> stopTags = null;
        {
            stopTags = new HashSet<Tag>();
            for (String tagStr : STOP_TAG_STR.split("[,]")) {
                stopTags.add(POS.resolveTag(tagStr));
            }
            if (stopTags.size() == 0) {
                stopTags = null;
            }
        }
        Analyzer analyzer = null;
        TokenStream stream = null;
        try {
            String text = TEXT_STR;
            ////////////////////////////////////////////////////////////////////////////////
            //decompoundMode = DecompoundMode.NONE;
            //text = "상품명분석기1234abcd12ab34cd테스트중입니다";
            ////////////////////////////////////////////////////////////////////////////////
            Reader input = new StringReader(text);
            analyzer = new ProductNameAnalyzer(userDictionary, decompoundMode, stopTags, false, false);
            stream = analyzer.tokenStream("", input);
            stream.reset();
            stream.clearAttributes();
            int pos = 0;
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            PositionIncrementAttribute pincAtt = stream.addAttribute(PositionIncrementAttribute.class);
            OffsetAttribute offsAtt = stream.addAttribute(OffsetAttribute.class);
            TokenAttribute tokenAtt = stream.addAttribute(TokenAttribute.class);

            Object[] types = { 
                POS.Tag.NNB, POS.Tag.NNBC, POS.Tag.NNG, POS.Tag.NNP,
                POS.Tag.VA, POS.Tag.VV, POS.Tag.VX,
                POS.Tag.SN, POS.Tag.SL
            };

            while (stream.incrementToken()) {
                Object type = null;
                Token token = tokenAtt.getToken();
                type = token.getLeftPOS();

                boolean visible = false;

                if (isIn(type, types)) {
                    visible = true;
                }

                if (visible) {
                    logger.debug("TERM:{} / {} / {}~{} / {}", termAtt, type, offsAtt.startOffset(),
                            offsAtt.endOffset(), pos += pincAtt.getPositionIncrement());
                } else {
                    logger.debug("TERM:{} / {}", termAtt, type);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            stream.close();
            analyzer.close();
        }
        assertTrue(true);
    }

    @Test
    public void testDictionaryLoadedTokenizerPerformance () throws Exception {
        /**
		 * 테스트방법 : 테스트 실행시 파라메터셋팅 필요
		 * -DPROP_TEST_MASSIVE_DATA=Y
         * -DPROP_TEST_TEXT_DATA=C:/..../data.txt"
		 * -DPROP_TEST_DICTIONARY_SETTING=C:/..../product_name_analysis.prop
         * 
         * DICTIONARY_SETTING 형식은 다음과 같다
         * analysis.product.dictionary.list=user,synonym,stop,space,compound,unit,unit_synonym,maker,brand,category,english
         * analysis.product.dictionary.type.user=SET
         * analysis.product.dictionary.tokenType.user=MAX
         * analysis.product.dictionary.filePath.user=C:/..../Product/dict/user.dict
         * analysis.product.dictionary.type.synonym=SYNONYM
         * analysis.product.dictionary.tokenType.synonym=NONE
         * analysis.product.dictionary.filePath.synonym=C:/..../Product/dict/synonym.dict
         * analysis.product.dictionary.type.stop=SET
         * analysis.product.dictionary.tokenType.stop=MAX
         * analysis.product.dictionary.filePath.stop=C:/..../Product/dict/stop.dict
         * analysis.product.dictionary.type.space=SPACE
         * analysis.product.dictionary.tokenType.space=MIN
         * analysis.product.dictionary.filePath.space=C:/..../Product/dict/space.dict
         * analysis.product.dictionary.type.compound=COMPOUND
         * analysis.product.dictionary.tokenType.compound=MID
         * analysis.product.dictionary.filePath.compound=C:/..../Product/dict/compound.dict
         * analysis.product.dictionary.type.unit=SET
         * analysis.product.dictionary.tokenType.unit=HIGH
         * analysis.product.dictionary.filePath.unit=C:/..../Product/dict/unit.dict
         * analysis.product.dictionary.type.unit_synonym=SYNONYM_2WAY
         * analysis.product.dictionary.tokenType.unit_synonym=HIGH
         * analysis.product.dictionary.filePath.unit_synonym=C:/..../Product/dict/unit_synonym.dict
         * analysis.product.dictionary.type.maker=CUSTOM
         * analysis.product.dictionary.tokenType.maker=NONE
         * analysis.product.dictionary.filePath.maker=C:/..../Product/dict/maker.dict
         * analysis.product.dictionary.type.brand=CUSTOM
         * analysis.product.dictionary.tokenType.brand=NONE
         * analysis.product.dictionary.filePath.brand=C:/..../Product/dict/brand.dict
         * analysis.product.dictionary.type.category=CUSTOM
         * analysis.product.dictionary.tokenType.category=NONE
         * analysis.product.dictionary.filePath.category=C:/..../Product/dict/category.dict
         * analysis.product.dictionary.type.english=SET
         * analysis.product.dictionary.tokenType.english=MAX
         * analysis.product.dictionary.filePath.english=C:/..../Product/dict/english.dict
		 */
        // --------------------------------------------------------------------------------
        // FASTCAT 기반 퍼포먼스 테스트 (FOR-DOCUMENT / RULE 제거)
        // TOTAL ANALYZED COUNT 5000 / MIN:0.033ms / MAX:21.879ms / TOTAL:601.486ms / AVERAGE:0.12ms
        // TOTAL ANALYZED COUNT 10000 / MIN:0.019ms / MAX:9.049ms / TOTAL:387.167ms / AVERAGE:0.038ms
        // TOTAL ANALYZED COUNT 15000 / MIN:0.02ms / MAX:7.97ms / TOTAL:273.98ms / AVERAGE:0.018ms
        // TOTAL ANALYZED COUNT 20000 / MIN:0.016ms / MAX:5.145ms / TOTAL:251.973ms / AVERAGE:0.012ms
        // TOTAL ANALYZED COUNT 25000 / MIN:0.016ms / MAX:5.708ms / TOTAL:233.635ms / AVERAGE:0.009ms
        // TOTAL ANALYZED COUNT 30000 / MIN:0.016ms / MAX:5.343ms / TOTAL:240.181ms / AVERAGE:0.008ms
        // TOTAL ANALYZED COUNT 35000 / MIN:0.016ms / MAX:5.408ms / TOTAL:245.383ms / AVERAGE:0.007ms
        // TOTAL ANALYZED COUNT 40000 / MIN:0.016ms / MAX:5.557ms / TOTAL:235.818ms / AVERAGE:0.005ms
        // TOTAL ANALYZED COUNT 45000 / MIN:0.017ms / MAX:5.522ms / TOTAL:238.599ms / AVERAGE:0.005ms
        // TOTAL ANALYZED COUNT 50000 / MIN:0.017ms / MAX:5.048ms / TOTAL:245.416ms / AVERAGE:0.004ms
        // --------------------------------------------------------------------------------
        // 노리 기반 퍼포먼스 테스트
        // TOTAL ANALYZED COUNT 5000 / MIN:0.293ms / MAX:362.722ms / TOTAL:6114.84ms / AVERAGE:1.222ms
        // TOTAL ANALYZED COUNT 10000 / MIN:0.347ms / MAX:4.973ms / TOTAL:5750.179ms / AVERAGE:0.575ms
        // TOTAL ANALYZED COUNT 15000 / MIN:0.314ms / MAX:6.3ms / TOTAL:5564.782ms / AVERAGE:0.37ms
        // TOTAL ANALYZED COUNT 20000 / MIN:0.349ms / MAX:5.302ms / TOTAL:5696.846ms / AVERAGE:0.284ms
        // TOTAL ANALYZED COUNT 25000 / MIN:0.338ms / MAX:5.711ms / TOTAL:5575.568ms / AVERAGE:0.223ms
        // TOTAL ANALYZED COUNT 30000 / MIN:0.353ms / MAX:5.855ms / TOTAL:5924.865ms / AVERAGE:0.197ms
        // TOTAL ANALYZED COUNT 35000 / MIN:0.275ms / MAX:5.894ms / TOTAL:5749.822ms / AVERAGE:0.164ms
        // TOTAL ANALYZED COUNT 40000 / MIN:0.307ms / MAX:5.5ms / TOTAL:5750.924ms / AVERAGE:0.143ms
        // TOTAL ANALYZED COUNT 45000 / MIN:0.362ms / MAX:5.825ms / TOTAL:5820.408ms / AVERAGE:0.129ms
        // TOTAL ANALYZED COUNT 50000 / MIN:0.303ms / MAX:4.885ms / TOTAL:5917.584ms / AVERAGE:0.118ms
        // --------------------------------------------------------------------------------
        Properties prop = new Properties(0);
        File propFile = null;
        File testTextFile = null;
        BufferedReader reader = null;
        try { propFile = new File(System.getProperty("PROP_TEST_DICTIONARY_SETTING")); } catch (Exception ignore) { }
        try { testTextFile = new File(System.getProperty("PROP_TEST_TEXT_DATA")); } catch (Exception ignore) { }
        // 빌드시 자동 테스트 수행을 막는다
        if (!"Y".equals(System.getProperty("PROP_TEST_MASSIVE_DATA")) || !(propFile != null && propFile.exists()) || 
            !(testTextFile != null && testTextFile.exists())) {
            return;
        }
        try {
            reader = new BufferedReader(new FileReader(propFile));
            prop.load(reader);
        } finally {
            try { reader.close(); } catch (Exception ignore) { }
        }
        CommonDictionary commonDictionary = ProductNameTokenizerFactory.loadDictionary(null, prop);
        SystemDictionary userDictionary = commonDictionary.getSystemDictionary();
        DecompoundMode decompoundMode = DecompoundMode.NONE;
        Set<Tag> stopTags = null;
        {
            stopTags = new HashSet<Tag>();
            for (String tagStr : STOP_TAG_STR.split("[,]")) {
                stopTags.add(POS.resolveTag(tagStr));
            }
            if (stopTags.size() == 0) {
                stopTags = null;
            }
        }
        Analyzer analyzer = null;
        TokenStream stream = null;
        double nanoTime = 0;
        double totalTime = 0;
        double minTime = Double.MAX_VALUE;
        double maxTime = Double.MIN_VALUE;
        StringBuilder sb = new StringBuilder();
        long count = 0;
        try {
            reader = new BufferedReader(new FileReader(testTextFile));
            for (String text; (text = reader.readLine()) != null;) {
                count++;
                nanoTime = System.nanoTime();
                Reader input = new StringReader(text);
                analyzer = new ProductNameAnalyzer(userDictionary, decompoundMode, stopTags, false, false);
                stream = analyzer.tokenStream("", input);
                stream.reset();
                stream.clearAttributes();
                CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

                sb.setLength(0);
                while (stream.incrementToken()) {
                    sb.append(termAtt).append(" ");
                }
                nanoTime = System.nanoTime() - nanoTime;
                nanoTime = (double) nanoTime / 1000000D;
                if (nanoTime < minTime) { minTime = nanoTime; }
                if (nanoTime > maxTime) { maxTime = nanoTime; }
                totalTime += nanoTime;

                logger.trace("ANALYZED TAKES {}ms : \"{}\" -> \"{}\"", 
                    (double)((long) (nanoTime * 1000)) / 1000D , text, sb);

                if (count % 5000 == 0) {
                    logger.debug("TOTAL ANALYZED COUNT {} / MIN:{}ms / MAX:{}ms / TOTAL:{}ms / AVERAGE:{}ms", count, 
                        (double)((long) (minTime * 1000)) / 1000D,
                        (double)((long) (maxTime * 1000)) / 1000D,
                        (double)((long) (totalTime * 1000)) / 1000D,
                        (double)((long) (totalTime * 1000 / count)) / 1000D);
                    totalTime = 0;
                    minTime = Double.MAX_VALUE;
                    maxTime = Double.MIN_VALUE;

                }
                if (count > 50000) { break; }
            }
            stream.close();
            analyzer.close();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try { reader.close(); } catch (Exception ignore) { }
            try { stream.close(); } catch (Exception ignore) { }
            try { analyzer.close(); } catch (Exception ignore) { }
        }
        assertTrue(true);
    }

    public Tokenizer createTokenizer(Dictionary userDictionary, DecompoundMode decompoundMode,
        boolean discardPunctuation) {
        return new KoreanTokenizer(KoreanTokenizer.DEFAULT_TOKEN_ATTRIBUTE_FACTORY,
            userDictionary, decompoundMode, true, discardPunctuation);
    }
}
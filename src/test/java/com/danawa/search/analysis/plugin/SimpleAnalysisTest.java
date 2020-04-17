package com.danawa.search.analysis.plugin;

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
        File propFile = new File(System.getProperty("PROP_TEST_DICTIONARY_SETTING"));
        // 빌드시 자동 테스트 수행을 막는다
        if (!"Y".equals(System.getProperty("PROP_TEST_MASSIVE_DATA")) || !propFile.exists()) {
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

    public Tokenizer createTokenizer(Dictionary userDictionary, DecompoundMode decompoundMode,
        boolean discardPunctuation) {
        return new KoreanTokenizer(KoreanTokenizer.DEFAULT_TOKEN_ATTRIBUTE_FACTORY,
            userDictionary, decompoundMode, true, discardPunctuation);
    }
}
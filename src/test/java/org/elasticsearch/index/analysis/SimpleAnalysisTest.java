package org.elasticsearch.index.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.danawa.search.analysis.plugin.AnalysisProductNamePlugin;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizer.DecompoundMode;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.apache.lucene.analysis.ko.util.DictionaryBuilder;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.test.IndexSettingsModule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LuceneTestCase.SuppressSysoutChecks(bugUrl = "")
public class SimpleAnalysisTest extends LuceneTestCase {
    private static Logger logger = LoggerFactory.getLogger(SimpleAnalysisTest.class);
    //private static final String text = "아름다운이땅에금수강산에단군할아버지가터잡으시고홍익인간뜻으로나라세우니대대손손훌륭한인물도많아고구려세운동명왕백제온조왕알에서나온혁거세만주벌판달려라광개토대왕신라장군이사부백결선생떡방아삼천궁녀의자왕황산벌의계백맞서싸운관창역사는흐른다말목자른김유신통일문무왕원효대사해골물혜초천축국바다의왕자장보고발해대조영귀주대첩강감찬서희거란족무단정치정중부화포최무선죽림칠현김부식지눌국사조계종의천천태종대마도정벌이종무일편단심정몽주목화씨는문익점해동공자최충삼국유사일연역사는흐른다황금을보기를돌같이하라최영장군의말씀받들자황희정승맹사성과학장영실신숙주와한명회역사는안다십만양병이율곡주리이퇴계신사임당오죽헌잘싸운다곽재우조헌김시민나라구한이순신태정태세문단세사육신과생육신몸바쳐서논개행주치마권율역사는흐른다번쩍번쩍홍길동의적임꺽정대쪽같은삼학사어사박문수삼년공부한석봉단원풍속도방랑시인김삿갓지도김정호영조대왕신문고정조규장각목민심서정약용녹두장군전봉준순교김대건서화가무황진이못살겠다홍경래삼일천하김옥균안중근은애국이완용은매국역사는흐른다별헤는밤윤동주종두지석영삼십삼인손병희만세만세유관순도산안창호어린이날방정환이수일과심순애장군의아들김두한날자꾸나이상황소그림중섭역사는흐른다";
    private static final String text = "아름다운ABC이땅에123금수강산에a12단군할아버지가터잡으시고홍익인간뜻으로나라세우니대대손손훌륭한인물도많아고구려세운동명왕백제온조왕알에서나온혁거세";

    @Test
    public void longTextTest() throws Exception {
        final Settings settings = Settings.builder().put("index.analysis.analyzer.my_analyzer.type", "product_name")
                .put("index.analysis.analyzer.my_analyzer.stoptags", "NR, SP")
                .put("index.analysis.analyzer.my_analyzer.decompound_mode", "mixed").build();
        final Analyzer analyzer = createTestAnalysis(settings);
        final TokenStream stream = analyzer.tokenStream("", text);
        final CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
        final TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
        final PositionIncrementAttribute pincAtt = stream.getAttribute(PositionIncrementAttribute.class);
        final OffsetAttribute offsAtt = stream.getAttribute(OffsetAttribute.class);
        logger.debug("START");
        stream.reset();
        stream.clearAttributes();
        int pos = 0;
        while (stream.incrementToken()) {
            logger.debug("TERM:{} / {} / {}~{} / {}", termAtt, typeAtt.type(), offsAtt.startOffset(),
                    offsAtt.endOffset(), pos += pincAtt.getPositionIncrement());
        }
        assertTrue(true);
    }

    @Test
    public void simpleTokenizerTest() throws Exception {
        UserDictionary userDictionary = null;
        DecompoundMode decompoundMode = null;
        boolean discardPunctuation = false;
        Tokenizer tokenizer = null;
        try {
            tokenizer = new KoreanTokenizer(KoreanTokenizer.DEFAULT_TOKEN_ATTRIBUTE_FACTORY, userDictionary,
                decompoundMode, false, discardPunctuation);
            Reader input = new StringReader(text);
            tokenizer.setReader(input);
            tokenizer.reset();
            CharTermAttribute termAtt = tokenizer.addAttribute(CharTermAttribute.class);
            while(tokenizer.incrementToken()) {
                logger.debug("TOKEN:{}", termAtt);
            }
        } finally {
            tokenizer.close();
        }
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

    public Analyzer createTestAnalysis(Settings analysisSettings) throws IOException {
        InputStream dict = SimpleAnalysisTest.class.getResourceAsStream("user_dict.txt");
        if (logger.isTraceEnabled()) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(dict));
            for (String rl; (rl = reader.readLine()) != null;) {
                logger.debug("DICT:{}", rl);
            }
            reader.close();
            dict = SimpleAnalysisTest.class.getResourceAsStream("user_dict.txt");
        }

        final Path home = createTempDir();
        final Path config = home.resolve("config");
        final Path configPath = null;

        Files.createDirectory(config);
        Files.copy(dict, config.resolve("user_dict.txt"));

        final Settings settings = Settings.builder().put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(Environment.PATH_HOME_SETTING.getKey(), home).put(analysisSettings).build();

        final AnalysisProductNamePlugin plugins = new AnalysisProductNamePlugin();

        final Settings actualSettings;
        if (settings.get(IndexMetaData.SETTING_VERSION_CREATED) == null) {
            actualSettings = Settings.builder().put(settings)
                    .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT).build();
        } else {
            actualSettings = settings;
        }
        final IndexSettings indexSettings = IndexSettingsModule.newIndexSettings("test", actualSettings);
        final AnalysisRegistry analysisRegistry = new AnalysisModule(new Environment(actualSettings, configPath),
                Arrays.asList(plugins)).getAnalysisRegistry();

        final IndexAnalyzers indexAnalyzers = analysisRegistry.build(indexSettings);
		//Map<String, TokenFilterFactory> tokenFilterFactories = analysisRegistry.buildTokenFilterFactories(indexSettings);
		//Map<String, TokenizerFactory> tokenizerFactories = analysisRegistry.buildTokenizerFactories(indexSettings);
		//Map<String, CharFilterFactory> charFilterFactories = analysisRegistry.buildCharFilterFactories(indexSettings);
		
		return indexAnalyzers.get("my_analyzer");
    }
}
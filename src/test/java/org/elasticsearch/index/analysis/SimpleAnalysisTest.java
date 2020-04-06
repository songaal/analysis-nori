package org.elasticsearch.index.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
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
import org.elasticsearch.plugin.analysis.nori.AnalysisNoriPlugin;
import org.elasticsearch.test.IndexSettingsModule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAnalysisTest extends LuceneTestCase  {
	private static Logger logger = LoggerFactory.getLogger(SimpleAnalysisTest.class);

	@Test public void longTextTest() throws Exception {
		//String text = "아름다운이땅에금수강산에단군할아버지가터잡으시고홍익인간뜻으로나라세우니대대손손훌륭한인물도많아고구려세운동명왕백제온조왕알에서나온혁거세만주벌판달려라광개토대왕신라장군이사부백결선생떡방아삼천궁녀의자왕황산벌의계백맞서싸운관창역사는흐른다말목자른김유신통일문무왕원효대사해골물혜초천축국바다의왕자장보고발해대조영귀주대첩강감찬서희거란족무단정치정중부화포최무선죽림칠현김부식지눌국사조계종의천천태종대마도정벌이종무일편단심정몽주목화씨는문익점해동공자최충삼국유사일연역사는흐른다황금을보기를돌같이하라최영장군의말씀받들자황희정승맹사성과학장영실신숙주와한명회역사는안다십만양병이율곡주리이퇴계신사임당오죽헌잘싸운다곽재우조헌김시민나라구한이순신태정태세문단세사육신과생육신몸바쳐서논개행주치마권율역사는흐른다번쩍번쩍홍길동의적임꺽정대쪽같은삼학사어사박문수삼년공부한석봉단원풍속도방랑시인김삿갓지도김정호영조대왕신문고정조규장각목민심서정약용녹두장군전봉준순교김대건서화가무황진이못살겠다홍경래삼일천하김옥균안중근은애국이완용은매국역사는흐른다별헤는밤윤동주종두지석영삼십삼인손병희만세만세유관순도산안창호어린이날방정환이수일과심순애장군의아들김두한날자꾸나이상황소그림중섭역사는흐른다";
		String text = "아름다운ABC이땅에123금수강산에a12단군할아버지가터잡으시고홍익인간뜻으로나라세우니대대손손훌륭한인물도많아고구려세운동명왕백제온조왕알에서나온혁거세";

        Settings settings = Settings.builder()
            .put("index.analysis.analyzer.my_analyzer.type", "nori")
            .put("index.analysis.analyzer.my_analyzer.stoptags", "NR, SP")
            .put("index.analysis.analyzer.my_analyzer.decompound_mode", "mixed")
            .build();
         Analyzer analyzer = createTestAnalysis(settings);
         TokenStream stream = analyzer.tokenStream("", text);
         CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
         TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
         PositionIncrementAttribute pincAtt = stream.getAttribute(PositionIncrementAttribute.class);
         OffsetAttribute offsAtt = stream.getAttribute(OffsetAttribute.class);
         logger.debug("START");
         stream.reset();
		 stream.clearAttributes();
		 int pos = 0;
         while(stream.incrementToken()) {
        	 logger.debug("TERM:{} / {} / {}~{} / {}", termAtt, typeAtt.type(), offsAtt.startOffset(), offsAtt.endOffset(), pos += pincAtt.getPositionIncrement());
         }
		assertTrue(true);
	}
	
    public Analyzer createTestAnalysis(Settings analysisSettings) throws IOException {
        InputStream dict = SimpleAnalysisTest.class.getResourceAsStream("user_dict.txt");
        if (logger.isTraceEnabled()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(dict));
			for (String rl; (rl = reader.readLine()) != null;) { logger.debug("DICT:{}", rl); }
			reader.close();
			dict = SimpleAnalysisTest.class.getResourceAsStream("user_dict.txt");
        }
        
        Path home = createTempDir();
        Path config = home.resolve("config");
        Path configPath = null;
        
        Files.createDirectory(config);
        Files.copy(dict, config.resolve("user_dict.txt"));

        Settings settings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
            .put(Environment.PATH_HOME_SETTING.getKey(), home)
            .put(analysisSettings)
            .build();
        
        AnalysisNoriPlugin plugins = new AnalysisNoriPlugin();
        
        final Settings actualSettings;
        if (settings.get(IndexMetaData.SETTING_VERSION_CREATED) == null) {
            actualSettings = Settings.builder().put(settings).put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT).build();
        } else {
            actualSettings = settings;
        }
        final IndexSettings indexSettings = IndexSettingsModule.newIndexSettings("test", actualSettings);
		final AnalysisRegistry analysisRegistry =
                new AnalysisModule(new Environment(actualSettings, configPath), Arrays.asList(plugins)).getAnalysisRegistry();
        
		IndexAnalyzers indexAnalyzers = analysisRegistry.build(indexSettings);
		//Map<String, TokenFilterFactory> tokenFilterFactories = analysisRegistry.buildTokenFilterFactories(indexSettings);
		//Map<String, TokenizerFactory> tokenizerFactories = analysisRegistry.buildTokenizerFactories(indexSettings);
		//Map<String, CharFilterFactory> charFilterFactories = analysisRegistry.buildCharFilterFactories(indexSettings);
		
		return indexAnalyzers.get("my_analyzer");
    }
}
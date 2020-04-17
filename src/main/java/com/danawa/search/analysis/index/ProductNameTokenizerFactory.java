package com.danawa.search.analysis.index;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import com.danawa.search.analysis.dict.CommonDictionary;
import com.danawa.search.analysis.dict.CompoundDictionary;
import com.danawa.search.analysis.dict.CustomDictionary;
import com.danawa.search.analysis.dict.InvertMapDictionary;
import com.danawa.search.analysis.dict.MapDictionary;
import com.danawa.search.analysis.dict.SetDictionary;
import com.danawa.search.analysis.dict.SourceDictionary;
import com.danawa.search.analysis.dict.SpaceDictionary;
import com.danawa.search.analysis.dict.SynonymDictionary;
import com.danawa.search.analysis.dict.SystemDictionary;
import com.danawa.search.analysis.dict.SystemDictionary.TokenType;
import com.danawa.search.analysis.dict.SystemDictionary.Type;
import com.danawa.search.analysis.dict.SystemDictionary.WordEntry;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.dict.Dictionary;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.analysis.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductNameTokenizerFactory extends AbstractTokenizerFactory {

    private static Logger logger = LoggerFactory.getLogger(ProductNameTokenizerFactory.class);

    private static final String dictionaryPath = "dict/";
    private static final String dictionarySuffix = ".dict";

    public static final String USER_DICT_PATH_OPTION = "user_dictionary";
    public static final String USER_DICT_RULES_OPTION = "user_dictionary_rules";

    private static final String ANALYSIS_PROP = "product_name_analysis.prop";
    private static final String ATTR_DICTIONARY_ID_LIST = "analysis.product.dictionary.list";
    private static final String ATTR_DICTIONARY_TYPE = "analysis.product.dictionary.type";
    private static final String ATTR_DICTIONARY_TOKEN_TYPE = "analysis.product.dictionary.tokenType";
    private static final String ATTR_DICTIONARY_FILE_PATH = "analysis.product.dictionary.filePath";

    private final Dictionary userDictionary;
    private final KoreanTokenizer.DecompoundMode decompoundMode;
    private final boolean discardPunctuation;

    public ProductNameTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, settings, name);
        decompoundMode = getMode(settings);
        userDictionary = getUserDictionary(env, settings);
        discardPunctuation = settings.getAsBoolean("discard_punctuation", true);
    }

    public static Dictionary getUserDictionary(Environment env, Settings settings) {
        if (settings.get(USER_DICT_PATH_OPTION) != null && settings.get(USER_DICT_RULES_OPTION) != null) {
            throw new IllegalArgumentException("It is not allowed to use [" + USER_DICT_PATH_OPTION + "] in conjunction"
                    + " with [" + USER_DICT_RULES_OPTION + "]");
        }
        List<String> ruleList = Analysis.getWordList(env, settings, USER_DICT_PATH_OPTION, USER_DICT_RULES_OPTION,
                true);
        StringBuilder sb = new StringBuilder();
        if (ruleList == null || ruleList.isEmpty()) {
            return null;
        }
        for (String line : ruleList) {
            sb.append(line).append(System.lineSeparator());
        }
        try (Reader rulesReader = new StringReader(sb.toString())) {
            return UserDictionary.open(rulesReader);
        } catch (IOException e) {
            throw new ElasticsearchException("failed to load product-name user dictionary", e);
        }
    }

    public static KoreanTokenizer.DecompoundMode getMode(Settings settings) {
        KoreanTokenizer.DecompoundMode mode = KoreanTokenizer.DEFAULT_DECOMPOUND;
        String modeSetting = settings.get("decompound_mode", null);
        if (modeSetting != null) {
            mode = KoreanTokenizer.DecompoundMode.valueOf(modeSetting.toUpperCase(Locale.ENGLISH));
        }
        return mode;
    }

    @Override
    public Tokenizer create() {
        return new KoreanTokenizer(KoreanTokenizer.DEFAULT_TOKEN_ATTRIBUTE_FACTORY, userDictionary, decompoundMode,
                false, discardPunctuation);
    }

    private File getDictionaryFile(Properties prop, Environment env, String dictionaryId) {
        File ret = null;
        //속성에서 발견되면 속성내부 경로를 사용해 파일을 얻어오며, 그렇지 않은경우 지정된 경로에서 사전파일을 얻어온다
        String attribute = prop.getProperty(ATTR_DICTIONARY_FILE_PATH + "." + dictionaryId).trim();
        ret = new File(attribute);
        if (attribute == null || !ret.exists()) {
            ret = new File(new File(env.configFile().toFile(), dictionaryPath), dictionaryId + dictionarySuffix);
        }
        return ret;
    }

    private Type getType(Properties prop, String dictionaryId) {
        Type ret = null;
        String attribute = prop.getProperty(ATTR_DICTIONARY_TYPE + "." + dictionaryId).trim();
        for (Type type : Type.values()) {
            if (type.name().equalsIgnoreCase(attribute)) {
                ret = type;
                break;
            }
        }
        return ret;
    }

    private TokenType getTokenType(Properties prop, String dictionaryId) {
        TokenType ret = null;
        String attribute = prop.getProperty(ATTR_DICTIONARY_TOKEN_TYPE + "." + dictionaryId).trim();
        for (TokenType tokenType : TokenType.values()) {
            if (tokenType.name().equalsIgnoreCase(attribute)) {
                ret = tokenType;
                break;
            }
        }
        return ret;
    }

    private int getCost(TokenType tokenType) {
        int ret = SystemDictionary.DEFAULT_WORD_COST_LOW;
        if (tokenType == TokenType.HIGH) {
            ret = SystemDictionary.DEFAULT_WORD_COST_HIGH;
        } else if (tokenType == TokenType.MID) {
            ret = SystemDictionary.DEFAULT_WORD_COST_MID;
        }
        return ret;
    }

    protected CommonDictionary loadDictionary(Environment env, Settings settings) {
        /**
         * 기본셋팅. 
         * ${ELASTICSEARCH}/config/product_name_analysis.prop 파일을 사용하도록 한다
         * NORI 기분석 사전은 기본적으로(수정불가) 사용하되 사용자 사전을 활용하여
         * 커스터마이징 하도록 한다.
         * 우선은 JAXB 마샬링 구조를 사용하지 않고 Properties 를 사용하도록 한다.
         **/

		SystemDictionary dictionary = null;
		CommonDictionary commonDictionary = null;
        List<String> idList = new ArrayList<>();
        Properties dictProp = new Properties();
        Reader reader = null;
        try {
            reader = new FileReader(new File(env.configFile().toFile(), ANALYSIS_PROP));
            dictProp.load(reader);
            String idStr = dictProp.getProperty(ATTR_DICTIONARY_ID_LIST);
            if (idStr != null) {
                for (String id : idStr.split("[,]")) { idList.add(id.trim()); }
            }
        } catch (IOException e) {
        } finally {
            try { reader.close(); } catch (Exception ignore) { }
        }
		
		if (idList != null) {

            dictionary = new SystemDictionary();
			commonDictionary = new CommonDictionary(dictionary);
			
            Set<WordEntry> entries = null;
            for (String dictionaryId : idList) {
                Type type = getType(dictProp, dictionaryId);
                TokenType tokenType = getTokenType(dictProp, dictionaryId);
                File dictFile = getDictionaryFile(dictProp, env, dictionaryId);
                SourceDictionary<?> sourceDictionary = null;
				
                if (type == Type.SET) {
					SetDictionary setDictionary = new SetDictionary(dictFile);
					if(tokenType != null){
						entries = commonDictionary.appendDictionary(setDictionary.set(), getCost(tokenType), entries);
					}
					sourceDictionary = setDictionary;
                } else if (type == Type.MAP) {
					MapDictionary mapDictionary = new MapDictionary(dictFile);
					if(tokenType != null){
						entries = commonDictionary.appendDictionary(mapDictionary.map().keySet(), getCost(tokenType), entries);
					}
					sourceDictionary = mapDictionary;
                } else if (type == Type.SYNONYM || type == Type.SYNONYM_2WAY) {
					SynonymDictionary synonymDictionary = new SynonymDictionary(dictFile);
					if(tokenType != null){
						entries = commonDictionary.appendDictionary(synonymDictionary.getWordSet(), getCost(tokenType), entries);
					}
					sourceDictionary = synonymDictionary;
                } else if (type == Type.SPACE) {
					SpaceDictionary spaceDictionary = new SpaceDictionary(dictFile);
					if(tokenType != null){
						entries = commonDictionary.appendDictionary(spaceDictionary.map().keySet(), getCost(tokenType), entries);
					}
                    sourceDictionary = spaceDictionary;
					// Map map = new HashMap<CharSequence, PreResult<CharSequence>>();
					// for(Entry<CharSequence, CharSequence[]> e : spaceDictionary.map().entrySet()){
					// 	PreResult preResult = new PreResult<T>();
					// 	preResult.setResult(e.getValue());
					// 	map.put(e.getKey(), preResult);
					// }
					// commonDictionary.setPreDictionary(map);
                } else if (type == Type.CUSTOM) {
					CustomDictionary customDictionary = new CustomDictionary(dictFile);
					if(tokenType != null){
						entries = commonDictionary.appendDictionary(customDictionary.getWordSet(), getCost(tokenType), entries);
					}
					sourceDictionary = customDictionary;
                } else if (type == Type.INVERT_MAP) {
                    InvertMapDictionary invertMapDictionary = new InvertMapDictionary(dictFile);
                    if(tokenType != null){
						entries = commonDictionary.appendDictionary(invertMapDictionary.map().keySet(), getCost(tokenType), entries);
                    }
                    sourceDictionary = invertMapDictionary;
                } else if (type == Type.COMPOUND) {
					CompoundDictionary compoundDictionary = new CompoundDictionary(dictFile);
					if(tokenType != null){
						entries = commonDictionary.appendDictionary(compoundDictionary.map().keySet(), getCost(tokenType), entries);
					}
					sourceDictionary = compoundDictionary;
                } else if (type == Type.SYSTEM) {
					//ignore
                } else {
                    logger.error("Unknown Dictionary type > {}", type);
				}
                logger.info("Dictionary {} is loaded. tokenType[{}] ", dictionaryId, tokenType);
				//add dictionary
                if (sourceDictionary != null) {
				 	commonDictionary.addDictionary(dictionaryId, sourceDictionary);
				}
            }
        }
		return commonDictionary;
	}
}
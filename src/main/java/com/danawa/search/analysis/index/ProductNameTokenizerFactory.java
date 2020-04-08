package com.danawa.search.analysis.index;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.analysis.Analysis;

public class ProductNameTokenizerFactory extends AbstractTokenizerFactory {
    private static final String USER_DICT_PATH_OPTION = "user_dictionary";
    private static final String USER_DICT_RULES_OPTION = "user_dictionary_rules";

    private final UserDictionary userDictionary;
    private final KoreanTokenizer.DecompoundMode decompoundMode;
    private final boolean discardPunctuation;

    public ProductNameTokenizerFactory (IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, settings, name);
        decompoundMode = getMode(settings);
        userDictionary = getUserDictionary(env, settings);
        discardPunctuation = settings.getAsBoolean("discard_punctuation", true);
    }

    public static UserDictionary getUserDictionary(Environment env, Settings settings) {
        if (settings.get(USER_DICT_PATH_OPTION) != null && settings.get(USER_DICT_RULES_OPTION) != null) {
            throw new IllegalArgumentException("It is not allowed to use [" + USER_DICT_PATH_OPTION + "] in conjunction" +
                " with [" + USER_DICT_RULES_OPTION + "]");
        }
        List<String> ruleList = Analysis.getWordList(env, settings, USER_DICT_PATH_OPTION, USER_DICT_RULES_OPTION, true);
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
        return new KoreanTokenizer(KoreanTokenizer.DEFAULT_TOKEN_ATTRIBUTE_FACTORY, userDictionary, decompoundMode, false,
            discardPunctuation);
    }
}
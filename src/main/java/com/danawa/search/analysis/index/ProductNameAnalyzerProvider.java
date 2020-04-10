package com.danawa.search.analysis.index;

import java.util.List;
import java.util.Set;

import com.danawa.search.analysis.productname.ProductNameAnalyzer;

import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilter;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.Analysis;

import static com.danawa.search.analysis.index.ProductNamePartOfSpeechStopFilterFactory.resolvePOSList;

public class ProductNameAnalyzerProvider extends AbstractIndexAnalyzerProvider<ProductNameAnalyzer> {
    private final ProductNameAnalyzer analyzer;

    public ProductNameAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
        final KoreanTokenizer.DecompoundMode mode = ProductNameTokenizerFactory.getMode(settings);
        final UserDictionary userDictionary = ProductNameTokenizerFactory.getUserDictionary(env, settings);
        final List<String> tagList = Analysis.getWordList(env, settings, "stoptags");
        final Set<POS.Tag> stopTags = tagList != null ? resolvePOSList(tagList) : KoreanPartOfSpeechStopFilter.DEFAULT_STOP_TAGS;
        analyzer = new ProductNameAnalyzer(userDictionary, mode, stopTags, false);
    }

    @Override
    public ProductNameAnalyzer get() {
        return analyzer;
    }
}
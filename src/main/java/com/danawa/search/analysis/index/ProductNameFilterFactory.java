package com.danawa.search.analysis.index;

import com.danawa.search.analysis.productname.ProductNameFilter;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

public class ProductNameFilterFactory extends AbstractTokenFilterFactory {

    public ProductNameFilterFactory (IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new ProductNameFilter(tokenStream);
    }
}
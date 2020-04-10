package com.danawa.search.analysis.productname;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.DictionaryToken;
import org.apache.lucene.analysis.ko.Token;
import org.apache.lucene.analysis.ko.tokenattributes.TokenAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class ProductNameFilter extends TokenFilter {
    private TokenAttribute tokenAtt;
    private TypeAttribute typeAtt;

    public ProductNameFilter(TokenStream in) {
        super(in);
        tokenAtt = in.addAttribute(TokenAttribute.class);
        typeAtt = in.addAttribute(TypeAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            Token token = tokenAtt.getToken();
            String type = "";
            if (token instanceof DictionaryToken) {
                DictionaryToken dictToken = (DictionaryToken) token;
                type = dictToken.getType().name();
            } else {
                type = token.getPOSType().name();
            }
            typeAtt.setType(type);
            return true;
        } else {
            return false;
        }
    }
}
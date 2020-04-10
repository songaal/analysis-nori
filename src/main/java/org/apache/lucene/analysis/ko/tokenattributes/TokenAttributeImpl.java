package org.apache.lucene.analysis.ko.tokenattributes;

import org.apache.lucene.analysis.ko.Token;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class TokenAttributeImpl extends AttributeImpl implements TokenAttribute {

    private Token token;

    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public void setToken(Token token) {
        this.token = token;
    }

    @Override
    public void clear() {
        token = null;
    }

    @Override
    public void reflectWith(AttributeReflector reflector) {
    }

    @Override
    public void copyTo(AttributeImpl target) {
    }
}
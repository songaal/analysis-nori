/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis.ko.tokenattributes;

import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.Token;
import org.apache.lucene.util.Attribute;

/**
 * Attribute for Token data
 * <p>
 * Note: in some cases this value may not be applicable, and will be null.
 * @lucene.experimental
 */
public interface TokenAttribute extends Attribute {

    /**
     * 상품명 분석에 필요한 속성들
     */
    //분석기타입
    public static final Object TYPE_TOKEN_KNOWN = KoreanTokenizer.Type.KNOWN;
    public static final Object TYPE_TOKEN_UNKNOWN = KoreanTokenizer.Type.UNKNOWN;
    public static final Object TYPE_TOKEN_USER = KoreanTokenizer.Type.USER;
    public static final Object TYPE_POS_COMPOUND = POS.Type.COMPOUND;
    public static final Object TYPE_POS_INFLECT = POS.Type.INFLECT;
    public static final Object TYPE_POS_MORPHEME = POS.Type.MORPHEME;
    public static final Object TYPE_POS_PREANALYSIS = POS.Type.PREANALYSIS;
    //문자별타입
    public static final Object TYPE_STR_ALPHA = "ALPHA";
    public static final Object TYPE_STR_NUMBER = "NUMBER";
    public static final Object TYPE_STR_NUMBER_TRANS = "NUMBER_TRANS";
    public static final Object TYPE_STR_ALPHANUM = "ALPHANUM";
    public static final Object TYPE_STR_SYMBOL = "SYMBOL";
    public static final Object TYPE_STR_ASCII = "ASCII";
    public static final Object TYPE_STR_UNICODE = "UNICODE";
    //명사류
    public static final Object TYPE_TAG_NNB = POS.Tag.NNB;
    public static final Object TYPE_TAG_NNBC = POS.Tag.NNBC;
    public static final Object TYPE_TAG_NNG = POS.Tag.NNG;
    public static final Object TYPE_TAG_NNP = POS.Tag.NNP;
    //동사,부사
    public static final Object TYPE_TAG_VA = POS.Tag.VA;
    public static final Object TYPE_TAG_VV = POS.Tag.VV;
    public static final Object TYPE_TAG_VX = POS.Tag.VX;
    //미등록어(숫자,영문)
    public static final Object TYPE_TAG_SN = POS.Tag.SN;
    public static final Object TYPE_TAG_SL = POS.Tag.SL;

    /**
     * Get the current token.
     */
    Token getToken();

    /**
     * Set the current token.
     */
    void setToken(Token token);
}
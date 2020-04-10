package com.danawa.search.analysis.productname;

import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilter;
import org.apache.lucene.analysis.ko.KoreanReadingFormFilter;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.KoreanTokenizer.DecompoundMode;
import org.apache.lucene.analysis.ko.dict.Dictionary;

public class ProductNameAnalyzer extends Analyzer {
  private final Dictionary userDict;
  private final KoreanTokenizer.DecompoundMode mode;
  private final Set<POS.Tag> stopTags;
  private final boolean outputUnknownUnigrams;

  /**
   * Creates a new ProductNameAnalyzer.
   */
  public ProductNameAnalyzer() {
    this(null, KoreanTokenizer.DEFAULT_DECOMPOUND, KoreanPartOfSpeechStopFilter.DEFAULT_STOP_TAGS, false);
  }

  /**
   * Creates a new ProductNameAnalyzer.
   *
   * @param userDict Optional: if non-null, user dictionary.
   * @param mode Decompound mode.
   * @param stopTags The set of part of speech that should be filtered.
   * @param outputUnknownUnigrams If true outputs unigrams for unknown words.
   */
  public ProductNameAnalyzer(Dictionary userDict, DecompoundMode mode, Set<POS.Tag> stopTags, boolean outputUnknownUnigrams) {
    super();
    this.userDict = userDict;
    this.mode = mode;
    this.stopTags = stopTags;
    this.outputUnknownUnigrams = outputUnknownUnigrams;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer tokenizer = new KoreanTokenizer(TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY, userDict, mode, outputUnknownUnigrams);
    TokenStream stream = new KoreanPartOfSpeechStopFilter(tokenizer, stopTags);
    stream = new KoreanReadingFormFilter(stream);
    stream = new LowerCaseFilter(stream);
    stream = new ProductNameFilter(stream);
    return new TokenStreamComponents(tokenizer, stream);
  }

  @Override
  protected TokenStream normalize(String fieldName, TokenStream in) {
    TokenStream result = new LowerCaseFilter(in);
    return result;
  }
}
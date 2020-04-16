package com.danawa.search.analysis.dict;

import java.io.File;
import java.util.List;

/**
 * Created by swsong on 2015. 7. 31..
 */
public class InvertMapDictionary extends MapDictionary {

    public InvertMapDictionary() {
        super();
    }

    public InvertMapDictionary(File file) {
        super(file);
    }

    @Override
    public void addEntry(String keyword, Object[] values, List<Object> columnList) {
        if (keyword == null) {
            return;
        }
        keyword = keyword.trim();
        if (keyword.length() == 0) {
            return;
        }
        CharSequence[] value = new CharSequence[] { keyword };
        for (int i = 0; i < values.length; i++) {
            map.put(String.valueOf(values[i]), value);
        }
    }
}
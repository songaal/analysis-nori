package com.danawa.search.analysis.dict;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.danawa.search.analysis.dict.SystemDictionary.WordEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonDictionary {
	protected static final Logger logger = LoggerFactory.getLogger(CommonDictionary.class);
	private Date createTime;
	
	private SystemDictionary systemDictionary;
	
	private Map<String, SourceDictionary<?>> dictionaryMap;
	
	public CommonDictionary(SystemDictionary systemDictionary) {
		this.systemDictionary = systemDictionary;
		dictionaryMap = new HashMap<>();
		createTime = new Date();
	}
	
	//systemDictionary를 재설정한다. dictionaryMap은 따로 외부에서 해주어야함.
	public void reset(CommonDictionary dictionary) {
		this.systemDictionary = dictionary.systemDictionary;
		this.createTime = dictionary.createTime;
	}
	// public List<E> find(CharSequence token) {
	// 	return systemDictionary.find(token);
	// }
	// public P findPreResult(CharSequence token) {
	// 	return systemDictionary.findPreResult(token);
	// }
	// public void setPreDictionary(Map<CharSequence, P> map){
	// 	systemDictionary.setPreDictionary(map);
	// }
	// public int size(){
	// 	return systemDictionary.size();
	// }
	
	public Object getDictionary(String dictionaryId) {
		return dictionaryMap.get(dictionaryId);
	}
	
	public Map<String, SourceDictionary<?>> getDictionaryMap() {
		return dictionaryMap;
	}

	public Object addDictionary(String dictionaryId, SourceDictionary<?> dictionary) {
		logger.debug("addDictionary {} : {}", dictionaryId, dictionary);
		return dictionaryMap.put(dictionaryId, dictionary);
	}

	public Set<WordEntry> appendDictionary(Set<CharSequence> keySet, int cost, Set<WordEntry> entries) {
		// int cost = SystemDictionary.DEFAULT_WORD_COST_MID;
		// if (SystemDictionary.COST_HIGH.equals(tokenType)) {
		// 	cost = SystemDictionary.DEFAULT_WORD_COST_HIGH;
		// } else if (SystemDictionary.COST_LOW.equals(tokenType)) {
		// 	cost = SystemDictionary.DEFAULT_WORD_COST_LOW;
		// }
		return SystemDictionary.appendEntries(keySet, cost, entries);
	}

	public void applyDictionary(Set<WordEntry> entries) throws IOException {
		SystemDictionary.build(systemDictionary, entries);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "] createTime=" + createTime + ", dictionaries = " + dictionaryMap.size();
	}
}
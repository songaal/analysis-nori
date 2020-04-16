package com.danawa.search.analysis.dict;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	public void appendDictionary(Set<CharSequence> keySet, String tokenType) {
		//systemDictionary.appendAdditionalNounEntry(keySet, tokenType);
	}

	public void applyDictionary() {

	}
	
	@Override
	public String toString() {
		// return getClass().getSimpleName() + "] createTime=" + createTime + ", entry = " + (systemDictionary != null ? systemDictionary.size() : 0) + ", dictionaries = "+dictionaryMap.size(); 
		return getClass().getSimpleName() + "] createTime=" + createTime + ", dictionaries = " + dictionaryMap.size();
	}
}
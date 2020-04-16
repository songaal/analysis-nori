package com.danawa.search.analysis.dict;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import com.danawa.fastcat.commons.io.DataInput;
import com.danawa.fastcat.commons.io.DataOutput;
import com.danawa.fastcat.commons.io.InputStreamDataInput;
import com.danawa.fastcat.commons.io.OutputStreamDataOutput;

public class CompoundDictionary extends MapDictionary {

	private Set<CharSequence> mainWordSet;
	private Set<CharSequence> wordSet;

	public CompoundDictionary() {
		super();
		if(mainWordSet == null) {
			mainWordSet = new HashSet<>();
		}
		if(wordSet == null) {
			wordSet = new HashSet<>();
		}
	}

	public CompoundDictionary(File file) {
		super(file);
		if(mainWordSet == null) {
			mainWordSet = new HashSet<>();
		}
		if(wordSet == null) {
			wordSet = new HashSet<>();
		}
	}

	public CompoundDictionary(InputStream is) {
		super(is);
		if(mainWordSet == null) {
			mainWordSet = new HashSet<>();
		}
		if(wordSet == null) {
			wordSet = new HashSet<>();
		}
	}

	public Set<CharSequence> getWordSet() {
		return wordSet;
	}
	
	public void setWordSet(Set<CharSequence> wordSet) {
		this.wordSet = wordSet;
	}

	public Set<CharSequence> getMainWordSet() {
		return mainWordSet;
	}

	public void setMainWordSet(Set<CharSequence> mainWordSet) {
		this.mainWordSet = mainWordSet;
	}

	public Set<CharSequence> getUnmodifiableWordSet() {
		return Collections.unmodifiableSet(wordSet);
	}
	public Set<CharSequence> getUnmodifiableMainWordSet() {
		return Collections.unmodifiableSet(mainWordSet);
	}

	@Override
	public void addEntry(String keyword, Object[] values, List<Object> columnSettingList) {
		ArrayList<CharSequence> list = new ArrayList<CharSequence>(4);
		CharSequence mainWord = null;
		if (keyword == null) {
			logger.error("Compound main keyword is null.");
			return;
		}
		if (values == null) {
			logger.error("Compound dictionary value is null.");
			return;
		}
		if (values.length == 0) {
			logger.error("Compound dictionary value is empty.");
			return;
		}
		keyword = keyword.trim();
		if (keyword.length() == 0) {
			logger.error("Compound main keyword is empty.");
			return;
		}
		mainWord = keyword;
		mainWordSet.add(mainWord);

		// 0번째에 복합명사들이 컴마 단위로 모두 입력되어 있으므로 [0]만 확인하면 된다.
		String valueString = values[0].toString();
		String[] nouns = valueString.split(",");
		for (int k = 0; k < nouns.length; k++) {
			String noun = nouns[k].trim();
			if (noun.length() > 0) {
				CharSequence word = noun;
				list.add(word);
				wordSet.add(word);
			}
		}
		CharSequence[] value = new CharSequence[list.size()];
		for (int j = 0; j < value.length; j++) {
			CharSequence word = list.get(j);
			value[j] = word;
		}
		if (value.length > 0) {
			map.put(mainWord, value);
		}
	}

	@Override
	@SuppressWarnings("resource")
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		DataOutput output = new OutputStreamDataOutput(out);
		// write size of synonyms
		output.writeVInt(mainWordSet.size());
		// write synonyms
		Iterator<CharSequence> mainWordIter = mainWordSet.iterator();
		while (mainWordIter.hasNext()) {
			CharSequence value = mainWordIter.next();
			output.writeUString(value.toString().toCharArray(), 0, value.length());
		}
		// write size of synonyms
		output.writeVInt(wordSet.size());
		// write synonyms
		Iterator<CharSequence> wordIter = wordSet.iterator();
		while (wordIter.hasNext()) {
			CharSequence value = wordIter.next();
			output.writeUString(value.toString().toCharArray(), 0, value.length());
		}
	}

	@Override
	@SuppressWarnings("resource")
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		DataInput input = new InputStreamDataInput(in);
		mainWordSet = new HashSet<>();
		int mainWordSize = input.readVInt();
		for (int entryInx = 0; entryInx < mainWordSize; entryInx++) {
			mainWordSet.add(new String(input.readUString()));
		}
		wordSet = new HashSet<>();
		int size = input.readVInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			wordSet.add(new String(input.readUString()));
		}
	}
	
	@Override
	public void reload(Object object) throws IllegalArgumentException {
		if (object != null && object instanceof CompoundDictionary) {
			super.reload(object);
			CompoundDictionary compoundDictionary = (CompoundDictionary) object;
			this.mainWordSet = compoundDictionary.getMainWordSet();
			this.wordSet = compoundDictionary.getWordSet();
		} else {
			throw new IllegalArgumentException("Reload dictionary argument error. argument = " + object);
		}
	}
}
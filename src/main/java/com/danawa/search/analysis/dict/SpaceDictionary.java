package com.danawa.search.analysis.dict;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.danawa.fastcat.commons.io.DataInput;
import com.danawa.fastcat.commons.io.DataOutput;
import com.danawa.fastcat.commons.io.InputStreamDataInput;
import com.danawa.fastcat.commons.io.OutputStreamDataOutput;

public class SpaceDictionary extends MapDictionary {

	private final static String DELIMITER = "\\s";
	private Set<CharSequence> wordSet;

	public SpaceDictionary() {
		super();
		if(wordSet == null) {
			wordSet = new HashSet<>();
		}
	}

	public SpaceDictionary(File file) {
		super(file);
	}

	public SpaceDictionary(InputStream is) {
		super(is);
	}

	public Set<CharSequence> getWordSet() {
		return wordSet;
	}

	public void setWordSet(Set<CharSequence> wordSet) {
		this.wordSet = wordSet;
	}

	public Set<CharSequence> getUnmodifiableWordSet() {
		return Collections.unmodifiableSet(wordSet);
	}
	
	private static final Pattern ptn = Pattern.compile("^[\\x00-\\x7F]*$");

	@Override
	public void addEntry(String word, Object[] values, List<Object> columnList) {
		String keyword = word.replaceAll(DELIMITER, "");
		wordSet.add(keyword);
		String[] list = word.split(DELIMITER);
		super.addEntry(keyword, list, columnList);
		for (int i = 0; i < list.length; i++) {
			String str = list[i].trim();
			//ASCII 골라내기
			if(!ptn.matcher(str).find()) {
				wordSet.add(list[i].trim());
			}
		}
	}

	@Override
	@SuppressWarnings("resource")
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		DataOutput output = new OutputStreamDataOutput(out);
		// write size of synonyms
		output.writeVInt(wordSet.size());

		// write synonyms
		Iterator<CharSequence> synonymIter = wordSet.iterator();
		for (; synonymIter.hasNext();) {
			CharSequence value = synonymIter.next();
			output.writeUString(value.toString().toCharArray(), 0, value.length());
		}
	}

	@Override
	@SuppressWarnings("resource")
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		DataInput input = new InputStreamDataInput(in);
		wordSet = new HashSet<>();
		int size = input.readVInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			wordSet.add(new String(input.readUString()));
		}
	}

	@Override
	public void reload(Object object) throws IllegalArgumentException {
		if (object != null && object instanceof SpaceDictionary) {
			super.reload(object);
			SpaceDictionary spaceDictionary = (SpaceDictionary) object;
			this.wordSet = spaceDictionary.getWordSet();

		} else {
			throw new IllegalArgumentException("Reload dictionary argument error. argument = " + object);
		}
	}
}
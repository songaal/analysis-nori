package com.danawa.search.analysis.dict;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.danawa.fastcat.commons.io.DataInput;
import com.danawa.fastcat.commons.io.DataOutput;
import com.danawa.fastcat.commons.io.InputStreamDataInput;
import com.danawa.fastcat.commons.io.OutputStreamDataOutput;

public class SetDictionary extends SourceDictionary<String> {

	private Set<CharSequence> set;

	public SetDictionary() {
		set = new HashSet<>();
	}

	public SetDictionary(Set<CharSequence> set) {
		this.set = set;
	}

	public SetDictionary(File file) {
		if (!file.exists()) {
			set = new HashSet<>();
			logger.error("사전파일이 존재하지 않습니다. file={}", file.getAbsolutePath());
			return;
		}
		InputStream is;
		try {
			is = new FileInputStream(file);
			readFrom(is);
			is.close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public SetDictionary(InputStream is) {
		try {
			readFrom(is);
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	@Override
	public void addEntry(String keyword, Object[] value, List<String> columnList) {
		keyword = keyword.trim();
		if (keyword.length() > 0) {
			CharSequence cv = new String(keyword).trim();
			set.add(cv);
		}
	}

	public Set<CharSequence> getUnmodifiableSet() {
		return Collections.unmodifiableSet(set);
	}

	public Set<CharSequence> set() {
		return set;
	}

	public void setSet(Set<CharSequence> set) {
		this.set = set;
	}

	public boolean contains(CharSequence key) {
		return set.contains(key);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		@SuppressWarnings("resource")
		DataOutput output = new OutputStreamDataOutput(out);
		Iterator<CharSequence> valueIter = set.iterator();
		// write size of set
		output.writeInt(set.size());
		// write values
		for (; valueIter.hasNext();) {
			CharSequence value = valueIter.next();
			output.writeString(value.toString());
		}
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		@SuppressWarnings("resource")
		DataInput input = new InputStreamDataInput(in);
		set = new HashSet<>();
		int size = input.readInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			set.add(input.readString());
		}
	}

	@Override
	public void addSourceLineEntry(String line) {
		addEntry(line, null, null);
	}

	@Override
	public void reload(Object object) throws IllegalArgumentException {
		if (object != null && object instanceof SetDictionary) {
			SetDictionary setDictionary = (SetDictionary) object;
			this.set = setDictionary.set();
		} else {
			throw new IllegalArgumentException("Reload dictionary argument error. argument = " + object);
		}
	}
}
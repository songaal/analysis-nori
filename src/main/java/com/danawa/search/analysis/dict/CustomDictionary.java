package com.danawa.search.analysis.dict;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.danawa.fastcat.commons.io.DataInput;
import com.danawa.fastcat.commons.io.DataOutput;
import com.danawa.fastcat.commons.io.InputStreamDataInput;
import com.danawa.fastcat.commons.io.OutputStreamDataOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDictionary extends SourceDictionary<Object> {
	private static Logger logger = LoggerFactory.getLogger(MapDictionary.class);
	private Set<CharSequence> wordSet;
	private Map<CharSequence, Object[]> map;
	
	public CustomDictionary() {
		super();
		map = new HashMap<>();
		wordSet = new HashSet<>();
	}

	public CustomDictionary(File file) {
		super();
		wordSet = new HashSet<>();
		if (!file.exists()) {
			map = new HashMap<>();
			logger.error("사전파일이 존재하지 않습니다. file={}", file.getAbsolutePath());
			return;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			readFrom(is);
			is.close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}
	
	public CustomDictionary(InputStream is) {
		super();
		try {
			readFrom(is);
		} catch (IOException e) {
			logger.error("", e);
		}
	}
	
	public Set<CharSequence> getWordSet() {
		return wordSet;
	}
	
	public Map<CharSequence, Object[]> getUnmodifiableMap() {
		return Collections.unmodifiableMap(map);
	}

	public Map<CharSequence, Object[]> map() {
		return map;
	}

	public void setMap(Map<CharSequence, Object[]> map) {
		this.map = map;
	}
	
	@Override
	@SuppressWarnings("resource")
	public void writeTo(OutputStream out) throws IOException {
		DataOutput output = (DataOutput) new OutputStreamDataOutput(out);
		Iterator<CharSequence> keySet = map.keySet().iterator();
		// write size of map
		output.writeVInt(map.size());
		// write key and value map
		for (; keySet.hasNext();) {
			// write key
			CharSequence key = keySet.next();
			output.writeUString(key.toString().toCharArray(), 0, key.length());
			
			// write values
			Object[] values = map.get(key);
			output.writeVInt(values.length);
			for (Object value : values) {
				if(value instanceof CharSequence) {
					output.writeByte(1);
					CharSequence v = (CharSequence) value;
					output.writeUString(v.toString().toCharArray(), 0, v.length());
				} else if(value instanceof CharSequence[]) {
					output.writeByte(2);
					CharSequence[] list = (CharSequence[]) value;
					output.writeVInt(list.length);
					for (CharSequence v : list) {
						output.writeUString(v.toString().toCharArray(), 0, v.length());
					}
				}
				
			}
		}
		output.writeVInt(wordSet.size());
		Iterator<CharSequence> iterator = wordSet.iterator();
		while (iterator.hasNext()) {
			CharSequence value = iterator.next();
			output.writeUString(value.toString().toCharArray(), 0, value.length());
		}
	}

	@Override
	@SuppressWarnings("resource")
	public void readFrom(InputStream in) throws IOException {
		DataInput input = new InputStreamDataInput(in);
		map = new HashMap<>();
		int size = input.readVInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			CharSequence key = new String(input.readUString());
			int valueLength = input.readVInt();
			Object[] values = new Object[valueLength];
			for (int valueInx = 0; valueInx < valueLength; valueInx++) {
				int type = input.readByte();
				if (type == 1) {
					values[valueInx] = new String(input.readUString());
				} else if (type == 2) {
					int len = input.readVInt();
					CharSequence[] list = new CharSequence[len];
					for (int j = 0; j < len; j++) {
						list[j] = new String(input.readUString());
					}
				}
			}
			map.put(key, values);
		}
		wordSet = new HashSet<>();
		size = input.readVInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			wordSet.add(new String(input.readUString()));
		}
	}

	@Override
	public void addEntry(String keyword, Object[] values, List<Object> columnSettingList) {
		if (keyword == null) {
			return;
		}
		keyword = keyword.trim();
		if (keyword.length() == 0) {
			return;
		}
		CharSequence cv = keyword.replaceAll("[ ]", "");
		Object[] list = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			String value = values[i].toString();
			// ColumnSetting columnSetting = columnSettingList.get(i);
			// String separator = columnSetting.getSeparator();
			// // separator가 존재하면 쪼개서 CharSequence[] 로 넣고 아니면 그냥 CharSequence 로 넣는다.
			// if (separator != null && separator.length() > 0) {
			// 	String[] e = value.split(separator);
			// 	// list[i] = new CharSequence[e.length];
			// 	CharSequence[] el = new CharSequence[e.length];
			// 	for (int j = 0; j < e.length; j++) {
			// 		el[j] = e[j].trim();
			// 		wordSet.add(el[j]);
			// 	}
			// 	list[i] = el;
			// } else { }
			CharSequence val = value;
			list[i] = val;
			wordSet.add(val);
		}
		map.put(cv, list);
	}
	
	@Override
	public void addSourceLineEntry(String line) {
		String[] kv = line.split("\t");
		if (kv.length == 1) {
			String value = kv[0].trim();
			addEntry(null, new Object[] { value }, null);
		} else if (kv.length == 2) {
			String keyword = kv[0].trim();
			String value = kv[1].trim();
			addEntry(keyword, new Object[] { value }, null);
		}
	}

	@Override
	public void reload(Object object) throws IllegalArgumentException {
		if (object != null && object instanceof CustomDictionary) {
			CustomDictionary customDictionary = (CustomDictionary) object;
			this.map = customDictionary.map();
		} else {
			throw new IllegalArgumentException("Reload dictionary argument error. argument = " + object);
		}
	}
	
	public void setWordSet(Set<CharSequence> wordSet) {
		this.wordSet = wordSet;
	}
}
package com.danawa.search.analysis.dict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.dict.CharacterDefinition;
import org.apache.lucene.analysis.ko.dict.Dictionary;
import org.apache.lucene.analysis.ko.dict.TokenInfoFST;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;

public class SystemDictionary implements Dictionary {

    public static enum Type {
        SYSTEM, SET, MAP, SYNONYM, SYNONYM_2WAY, SPACE, CUSTOM, INVERT_MAP, COMPOUND
    }

    public static enum TokenType {
        MAX, HIGH, MID, MIN
    }

    public static final int DEFAULT_WORD_COST_MAX   = -400000;
    public static final int DEFAULT_WORD_COST_HIGH  = -300000;
    public static final int DEFAULT_WORD_COST_MID   = -200000;
    public static final int DEFAULT_WORD_COST_MIN   = -100000;
    public static final short DEFAULT_LEFT_ID       = 1781;
    public static final short DEFAULT_RIGHT_ID      = 3533;
    public static final short DEFAULT_RIGHT_ID_T    = 3535;
    public static final short DEFAULT_RIGHT_ID_F    = 3534;

    // text -> wordID
    private TokenInfoFST fst;
    private short leftId;

    // length, length... indexed by compound ID or null for simple noun
    private int segmentations[][];
    private short[] rightIds;
    private int[] workCosts;

    public SystemDictionary() { }

    public static SystemDictionary open(Reader reader) throws IOException {
        return open(reader, DEFAULT_LEFT_ID, DEFAULT_RIGHT_ID, DEFAULT_RIGHT_ID_T, DEFAULT_RIGHT_ID_F);
    }

    public static SystemDictionary open(Reader reader, short leftId, short rightId, short rightIdT, short rightIdF) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        List<WordEntry> entries = new ArrayList<>();

        // text + optional segmentations
        while ((line = br.readLine()) != null) {
            // Remove comments
            line = line.replaceAll("#.*$", "");

            // Skip empty lines or comment lines
            if (line.trim().length() == 0) {
                continue;
            }
            entries.add(new WordEntry(line, DEFAULT_WORD_COST_MID));
        }

        if (entries.isEmpty()) {
            return null;
        } else {
            SystemDictionary dict = new SystemDictionary();
            build(dict, entries, leftId, rightId, rightIdT, rightIdF);
            return dict;
        }
    }

    public SystemDictionary(List<WordEntry> entries) throws IOException {
        this();
        build(this, entries);
    }

    public static void build(SystemDictionary dict, Collection<WordEntry> entries) throws IOException {
        build(dict, entries, DEFAULT_LEFT_ID, DEFAULT_RIGHT_ID, DEFAULT_RIGHT_ID_T, DEFAULT_RIGHT_ID_F);
    }

    public static void build(SystemDictionary dict, Collection<WordEntry> words, short leftId, short rightId, short rightIdT, short rightIdF) throws IOException {
        dict.leftId = leftId;
        final CharacterDefinition charDef = CharacterDefinition.getInstance();
        Set<WordEntry> entries = new TreeSet<>(new WordEntryComparator());
        entries.addAll(words);

        PositiveIntOutputs fstOutput = PositiveIntOutputs.getSingleton();
        Builder<Long> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);
        IntsRefBuilder scratch = new IntsRefBuilder();

        String lastToken = null;
        List<int[]> segmentations = new ArrayList<>(entries.size());
        List<Short> rightIds = new ArrayList<>(entries.size());
        dict.workCosts = new int[entries.size()];
        long ord = 0;
        for (WordEntry entry : entries) {
            String word = String.valueOf(entry.word);
            String[] splits = word.split("\\s+");
            String token = splits[0];
            if (token.equals(lastToken)) {
                continue;
            }
            char lastChar = word.charAt(word.length() - 1);
            if (charDef.isHangul(lastChar)) {
                if (charDef.hasCoda(lastChar)) {
                    rightIds.add(rightIdT);
                } else {
                    rightIds.add(rightIdF);
                }
            } else {
                rightIds.add(rightId);
            }

            if (splits.length == 1) {
                segmentations.add(null);
            } else {
                int[] length = new int[splits.length - 1];
                int offset = 0;
                for (int i = 1; i < splits.length; i++) {
                    length[i - 1] = splits[i].length();
                    offset += splits[i].length();
                }
                if (offset > token.length()) {
                    throw new IllegalArgumentException("Illegal user dictionary entry " + entry
                            + " - the segmentation is bigger than the surface form (" + token + ")");
                }
                segmentations.add(length);
            }

            // add mapping to FST
            scratch.grow(token.length());
            scratch.setLength(token.length());
            for (int i = 0; i < token.length(); i++) {
                scratch.setIntAt(i, (int) token.charAt(i));
            }
            fstBuilder.add(scratch.get(), ord);
            lastToken = token;
            dict.workCosts[(int) ord] = entry.cost;
            ord++;
        }
        dict.fst = new TokenInfoFST(fstBuilder.finish());
        dict.segmentations = segmentations.toArray(new int[segmentations.size()][]);
        dict.rightIds = new short[rightIds.size()];
        for (int i = 0; i < rightIds.size(); i++) {
            dict.rightIds[i] = rightIds.get(i);
        }
    }

    public TokenInfoFST getFST() {
        return fst;
    }

    @Override
    public int getLeftId(int wordId) {
        return leftId;
    }

    @Override
    public int getRightId(int wordId) {
        return rightIds[wordId];
    }

    @Override
    public int getWordCost(int wordId) {
        return workCosts[wordId];
    }

    @Override
    public POS.Type getPOSType(int wordId) {
        if (segmentations[wordId] == null) {
            return POS.Type.MORPHEME;
        } else {
            return POS.Type.COMPOUND;
        }
    }

    @Override
    public POS.Tag getLeftPOS(int wordId) {
        return POS.Tag.NNG;
    }

    @Override
    public POS.Tag getRightPOS(int wordId) {
        return POS.Tag.NNG;
    }

    @Override
    public String getReading(int wordId) {
        return null;
    }

    @Override
    public Morpheme[] getMorphemes(int wordId, char[] surfaceForm, int off, int len) {
        int[] segs = segmentations[wordId];
        if (segs == null) {
            return null;
        }
        int offset = 0;
        Morpheme[] morphemes = new Morpheme[segs.length];
        for (int i = 0; i < segs.length; i++) {
            morphemes[i] = new Morpheme(POS.Tag.NNG, new String(surfaceForm, off + offset, segs[i]));
            offset += segs[i];
        }
        return morphemes;
    }

    /**
     * Lookup words in text
     * 
     * @param chars text
     * @param off   offset into text
     * @param len   length of text
     * @return array of wordId
     */
    public List<Integer> lookup(CharSequence chars, int off, int len) throws IOException {
        List<Integer> result = new ArrayList<>();
        final FST.BytesReader fstReader = fst.getBytesReader();
        FST.Arc<Long> arc = new FST.Arc<>();
        int end = off + len;
        for (int startOffset = off; startOffset < end; startOffset++) {
            arc = fst.getFirstArc(arc);
            int output = 0;
            int remaining = end - startOffset;
            for (int i = 0; i < remaining; i++) {
                int ch = chars.charAt(startOffset + i);
                if (fst.findTargetArc(ch, arc, arc, i == 0, fstReader) == null) {
                    break; // continue to next position
                }
                output += arc.output().intValue();
                if (arc.isFinal()) {
                    final int finalOutput = output + arc.nextFinalOutput().intValue();
                    result.add(finalOutput);
                }
            }
        }
        return result;
    }

    public int contains(CharSequence chars) throws IOException {
        int ret = -1;
        final FST.BytesReader fstReader = fst.getBytesReader();
        FST.Arc<Long> arc = new FST.Arc<>();
        arc = fst.getFirstArc(arc);
        int output = 0;
        for (int inx = 0; inx < chars.length(); inx++) {
            int ch = chars.charAt(inx);
            if (fst.findTargetArc(ch, arc, arc, inx == 0, fstReader) == null) {
                break;
            }
            output += arc.output().intValue();
            if (arc.isFinal()) {
                ret = output + arc.nextFinalOutput().intValue();
                return ret;
            }
        }
        return ret;
    }

    public static Set<WordEntry> appendEntry(CharSequence word, int cost, Set<WordEntry> entries) {
        if (entries == null) {
            entries = new TreeSet<>(new WordEntryComparator());
        }
        entries.add(new WordEntry(word, cost));
        return entries;
    }

    public static Set<WordEntry> appendEntries(Collection<CharSequence> words, int cost, Set<WordEntry> entries) {
        for (CharSequence w : words) {
            entries = appendEntry(w, cost, entries);
        }
        return entries;
    }

    public static class WordEntry {
        public CharSequence word;
        public int cost;
        public WordEntry(CharSequence word, int cost) { this.word = word; this.cost = cost; }
        @Override public String toString() { 
            if (word != null) { return String.valueOf(word); }
            return null;
        }
    }

    private static class WordEntryComparator implements Comparator<WordEntry> {
        @Override public int compare(WordEntry e1, WordEntry e2) {
            CharSequence c1 = e1.word;
            CharSequence c2 = e2.word;
            String s1 = null;
            String s2 = null;
            if (c1 != null) {
                s1 = String.valueOf(c1).split("\\s+")[0];
            } else {
                if (c2 == null) {
                    // 둘 다 Null
                    return 0;
                } else {
                    // 선단어가 Null
                    return -1;
                }
            }
            if (c2 != null) {
                s2 = String.valueOf(c2).split("\\s+")[0];
            } else {
                // 후단어가 Null
                return 1;
            }
            return s1.compareTo(s2);
        }
    }
}
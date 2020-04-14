package com.danawa.search.analysis.dict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.dict.CharacterDefinition;
import org.apache.lucene.analysis.ko.dict.Dictionary;
import org.apache.lucene.analysis.ko.dict.TokenInfoFST;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;

public class SimpleDictionary implements Dictionary {
    // text -> wordID
    private final TokenInfoFST fst;
    private int workCost;
    private short leftId;

    // length, length... indexed by compound ID or null for simple noun
    private final int segmentations[][];
    private final short[] rightIds;

    public static SimpleDictionary open(int workCost, short leftId, short rightId, short rightIdT, short rightIdF, Reader reader) throws IOException {

        BufferedReader br = new BufferedReader(reader);
        String line = null;
        List<String> entries = new ArrayList<>();

        // text + optional segmentations
        while ((line = br.readLine()) != null) {
            // Remove comments
            line = line.replaceAll("#.*$", "");

            // Skip empty lines or comment lines
            if (line.trim().length() == 0) {
                continue;
            }
            entries.add(line);
        }

        if (entries.isEmpty()) {
            return null;
        } else {
            return new SimpleDictionary(workCost, leftId, rightId, rightIdT, rightIdF, entries);
        }
    }

    public SimpleDictionary(List<String> entries) throws IOException {
        this(/*workCost*/-100000, /*leftId*/(short) 1781, /*rightId*/(short) 3533, /*rightIdT*/(short) 3535, /*rightIdF*/(short) 3534, entries);
    }

    public SimpleDictionary(int workCost, short leftId, short rightId, short rightIdT, short rightIdF, List<String> entries) throws IOException {
        this.workCost = workCost;
        this.leftId = leftId;
        final CharacterDefinition charDef = CharacterDefinition.getInstance();
        entries.sort(Comparator.comparing(e -> e.split("\\s+")[0]));

        PositiveIntOutputs fstOutput = PositiveIntOutputs.getSingleton();
        Builder<Long> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);
        IntsRefBuilder scratch = new IntsRefBuilder();

        String lastToken = null;
        List<int[]> segmentations = new ArrayList<>(entries.size());
        List<Short> rightIds = new ArrayList<>(entries.size());
        long ord = 0;
        for (String entry : entries) {
            String[] splits = entry.split("\\s+");
            String token = splits[0];
            if (token.equals(lastToken)) {
                continue;
            }
            char lastChar = entry.charAt(entry.length() - 1);
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
            ord++;
        }
        this.fst = new TokenInfoFST(fstBuilder.finish());
        this.segmentations = segmentations.toArray(new int[segmentations.size()][]);
        this.rightIds = new short[rightIds.size()];
        for (int i = 0; i < rightIds.size(); i++) {
            this.rightIds[i] = rightIds.get(i);
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
        return workCost;
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

    public boolean contains(CharSequence chars) throws IOException {
        final FST.BytesReader fstReader = fst.getBytesReader();
        FST.Arc<Long> arc = new FST.Arc<>();
        arc = fst.getFirstArc(arc);
        for (int inx = 0; inx < chars.length(); inx++) {
            int ch = chars.charAt(inx);
            if (fst.findTargetArc(ch, arc, arc, inx == 0, fstReader) == null) {
                break;
            }
            if (arc.isFinal()) {
                return true;
            }
        }
        return false;
    }
}
package com.danawa.search.analysis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;

public class DictionaryBuilder {
    public void buildDictionary() throws Exception {
        File baseDir = null;
        File outputDir = null;
        {
            Class<?> cls = DictionaryBuilder.class;
            URL url = cls.getResource(cls.getSimpleName() + ".class");
            String path = url.getFile();
            String[] pkg = cls.getName().split("[.]");
            baseDir = new File(path);
            for (int inx = 0; inx < pkg.length; inx++) {
                baseDir = baseDir.getParentFile();
            }

            if ("classes".equals(baseDir.getName())) {
                String inputEncoding = "UTF-8";
                boolean normalizeEntries = true;
                baseDir = baseDir.getParentFile().getParentFile();
                outputDir = baseDir.getAbsoluteFile();
                baseDir = new File(baseDir, "src/main/rawdata/mecab-ko-dic");
                outputDir = new File(outputDir, "src/main/resources");

                /**
                 * NOTE: matrix.def 파일 용량이 매우 크므로 split 했으며
                 * 사전 컴파일 시 합쳐서 컴파일 하도록 함
                 */

                 {
                     BufferedReader reader = null;
                     Writer writer = null;
                     try {
                        File matrixFile = new File(baseDir, "matrix.def");
                        writer = new FileWriter(matrixFile);
                        for (int inx = 0;; inx++) {
                            File split = new File(baseDir, "matrix.def.split_" + inx);
                            if (!split.exists()) { break; }
                            reader = new BufferedReader(new InputStreamReader(new FileInputStream(split), inputEncoding));
                            for (String line; (line = reader.readLine()) != null;) {
                                writer.append(line).append("\n");
                            }
                            reader.close();
                        }
                    } finally {
                        try { reader.close(); } catch (Exception ignore) { }
                        try { writer.close(); } catch (Exception ignore) { }
                    }
                 }

                org.apache.lucene.analysis.ko.util.DictionaryBuilder.build(baseDir.toPath(), outputDir.toPath(),
                    inputEncoding, normalizeEntries);
            }
        }
    }

    public static void main(String[] arg) throws Exception {
        DictionaryBuilder builder = new DictionaryBuilder();
        builder.buildDictionary();
    }
}
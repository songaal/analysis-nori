package com.danawa.search.analysis.dict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;

import org.apache.lucene.analysis.ko.util.DictionaryBuilder;

/**
 * 개발시에만 사용가능한 클래스
 * 소스폴더를 기준으로 기분석사전 (은전한닢)을 노리분석기용 사전으로 컴파일
 * 런타임 라이브러리 빌드시에는 제외하도록 함
 **/
public class PreAnalysisDictionaryBuilder {
    public void buildDictionary() throws Exception {
        File baseDir = null;
        File outputDir = null;
        {
            Class<?> cls = PreAnalysisDictionaryBuilder.class;
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
                 **/
                if (baseDir.exists() && baseDir.isDirectory()) {
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
                    DictionaryBuilder.build(baseDir.toPath(), outputDir.toPath(),
                        inputEncoding, normalizeEntries);
                }
            }
        }
    }

    public static void main(String[] arg) throws Exception {
        PreAnalysisDictionaryBuilder builder = new PreAnalysisDictionaryBuilder();
        builder.buildDictionary();
    }
}
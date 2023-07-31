package org.example.Service;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;

@Service
public class SpellCheckerService {

    private final SpellChecker spellChecker;

    public SpellCheckerService() throws IOException {
        RAMDirectory directory = new RAMDirectory();
        spellChecker = new SpellChecker(directory);
        // Add words to the dictionary (can be loaded from a file or other sources)
        spellChecker.indexDictionary(new PlainTextDictionary(Paths.get("D:\\java\\CartoonSearchHelper\\words.txt")), new IndexWriterConfig(), true);
    }

    public String correctSpelling(String word) throws IOException {
        String[] suggestions = spellChecker.suggestSimilar(word, 1);
        return suggestions.length > 0 ? suggestions[0] : word;
    }
}

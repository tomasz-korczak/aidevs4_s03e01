package pl.tomaszko.s03e01.notes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.tomaszko.s03e01.scan.ClassifiedFile;

@Component
public class OperatorNotesIndexer {

    public record NotesEntry(int nr, String operatorNotes, List<ClassifiedFile> files) {}

    public List<NotesEntry> index(List<ClassifiedFile> validFiles) {
        Map<String, NotesEntry> byNotes = new LinkedHashMap<>();
        int nextNr = 1;
        for (ClassifiedFile file : validFiles) {
            if (file.getReading() == null) {
                continue;
            }
            String notes = file.getReading().getOperatorNotes();
            NotesEntry existing = byNotes.get(notes);
            if (existing == null) {
                List<ClassifiedFile> files = new ArrayList<>();
                files.add(file);
                byNotes.put(notes, new NotesEntry(nextNr++, notes, files));
            } else {
                existing.files().add(file);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(byNotes.values()));
    }
}

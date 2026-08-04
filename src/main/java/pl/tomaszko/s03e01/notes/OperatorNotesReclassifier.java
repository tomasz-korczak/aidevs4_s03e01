package pl.tomaszko.s03e01.notes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import pl.tomaszko.s03e01.notes.OperatorNotesIndexer.NotesEntry;
import pl.tomaszko.s03e01.report.InvalidFileReporter;
import pl.tomaszko.s03e01.scan.ClassifiedFile;
import pl.tomaszko.s03e01.scan.ScanSummary;

@Service
public class OperatorNotesReclassifier {

    private final InvalidFileReporter reporter;

    public OperatorNotesReclassifier(InvalidFileReporter reporter) {
        this.reporter = reporter;
    }

    public void apply(ScanSummary summary, List<NotesEntry> entries, Set<Integer> issueNrs) {
        if (issueNrs == null || issueNrs.isEmpty()) {
            return;
        }
        Map<Integer, NotesEntry> byNr = new HashMap<>();
        for (NotesEntry entry : entries) {
            byNr.put(entry.nr(), entry);
        }
        for (Integer nr : issueNrs) {
            NotesEntry entry = byNr.get(nr);
            if (entry == null) {
                continue;
            }
            for (ClassifiedFile file : List.copyOf(entry.files())) {
                summary.moveValidToOperator(file);
                reporter.operator(file.getBasename());
            }
        }
    }
}

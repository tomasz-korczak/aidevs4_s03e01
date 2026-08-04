package pl.tomaszko.s03e01.scan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanSummary {

    private int filesScanned;
    private int parseInvalid;
    private int scopeInvalid;
    private int operatorInvalid;
    private int valid;

    private final List<ClassifiedFile> validFiles = new ArrayList<>();
    private final List<ClassifiedFile> parseInvalidFiles = new ArrayList<>();
    private final List<ClassifiedFile> scopeInvalidFiles = new ArrayList<>();
    private final List<ClassifiedFile> operatorInvalidFiles = new ArrayList<>();

    public void incrementScanned() {
        filesScanned++;
    }

    public void addParseInvalid(ClassifiedFile file) {
        parseInvalidFiles.add(file);
        parseInvalid++;
    }

    public void addScopeInvalid(ClassifiedFile file) {
        scopeInvalidFiles.add(file);
        scopeInvalid++;
    }

    public void addValid(ClassifiedFile file) {
        validFiles.add(file);
        valid++;
    }

    public void moveValidToOperator(ClassifiedFile file) {
        if (!validFiles.remove(file)) {
            return;
        }
        valid--;
        file.setCategory(FileCategory.OPERATOR);
        operatorInvalidFiles.add(file);
        operatorInvalid++;
    }

    public List<ClassifiedFile> allInvalidFiles() {
        List<ClassifiedFile> invalid = new ArrayList<>();
        invalid.addAll(parseInvalidFiles);
        invalid.addAll(scopeInvalidFiles);
        invalid.addAll(operatorInvalidFiles);
        return invalid;
    }

    public int getFilesScanned() {
        return filesScanned;
    }

    public int getParseInvalid() {
        return parseInvalid;
    }

    public int getScopeInvalid() {
        return scopeInvalid;
    }

    public int getOperatorInvalid() {
        return operatorInvalid;
    }

    public int getValid() {
        return valid;
    }

    public List<ClassifiedFile> getValidFiles() {
        return Collections.unmodifiableList(validFiles);
    }

    public List<ClassifiedFile> getParseInvalidFiles() {
        return Collections.unmodifiableList(parseInvalidFiles);
    }

    public List<ClassifiedFile> getScopeInvalidFiles() {
        return Collections.unmodifiableList(scopeInvalidFiles);
    }

    public List<ClassifiedFile> getOperatorInvalidFiles() {
        return Collections.unmodifiableList(operatorInvalidFiles);
    }
}

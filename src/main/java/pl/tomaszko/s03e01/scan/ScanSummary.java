package pl.tomaszko.s03e01.scan;

public class ScanSummary {

    private int filesScanned;
    private int parseInvalid;
    private int scopeInvalid;
    private int valid;

    public void incrementScanned() {
        filesScanned++;
    }

    public void incrementParseInvalid() {
        parseInvalid++;
    }

    public void incrementScopeInvalid() {
        scopeInvalid++;
    }

    public void incrementValid() {
        valid++;
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

    public int getValid() {
        return valid;
    }
}

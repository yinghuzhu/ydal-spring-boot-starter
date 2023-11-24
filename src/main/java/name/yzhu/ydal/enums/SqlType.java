package name.yzhu.ydal.enums;

public enum  SqlType {
    SELECT("select"),
    INSERT("insert"),
    UPDATE("update"),
    DELETE("delete"),
    BATCH("batch"),
    UNKNOWN("unknown");

    private final String txt;

    SqlType(String txt) {
        this.txt = txt;
    }

    public String getTxt() {
        return txt;
    }
}

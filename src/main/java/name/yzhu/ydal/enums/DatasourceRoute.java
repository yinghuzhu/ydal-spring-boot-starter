package name.yzhu.ydal.enums;

public enum DatasourceRoute {
    MASTER("master"), SLAVE("slave");

    private final String txt;

    DatasourceRoute(String txt) {
        this.txt = txt;
    }

    public String getTxt() {
        return txt;
    }
}

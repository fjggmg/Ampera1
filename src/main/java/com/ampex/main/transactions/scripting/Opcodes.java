package com.ampex.main.transactions.scripting;


public enum Opcodes {
    VSIGVAL(52, "VSV"),
    VSIGVALS(53, "VSVS"),
    PUSHAA0(54, "PAA0"),
    BRANCH(56, "BRN"),
    SINT(61, "SI"),
    TIME(63, "TIM"),
    LHP1PCCSCK(65, "LHP1PCCS"),
    LHPCMEMSCK(66, "LHMPCS"),
    LOAMEMSCK(69, "LOAMS"),
    LPCCONSTSCK(70, "LPCCS"),
    VADDSAME(71, "VAS"),
    BRN0(72, "BRN0"),
    LOAMTMEMSCK(73, "LOATMS"),
    VRAT(74, "VRAT"),
    LOTS(76, "LOTS"),
    POUTSIZSCK(78, "POSS"),
    POUTSCK(79, "POS"),
    LOATSCK(82, "LOATS"),
    LOAMTTSCK(83, "LOATTS"),
    LOTTS(84, "LOTTS"),
    VERNETLOSS(86, "VNL"),
    BRANCH1(88, "BRN1"),
    BRANCH2(89, "BRN2"),
    VBINSIG(91, "VBS");
    private final int code;
    private final String keyword;

    Opcodes(int code, String keyword) {
        this.keyword = keyword;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getKeyword() {
        return keyword;
    }

    public static Opcodes byCode(int code) {
        for (Opcodes op : values()) {
            if (op.getCode() == code) return op;
        }
        return null;
    }

    public static Opcodes byKeyword(String keyword) {
        for (Opcodes op : values()) {
            if (op.getKeyword().equalsIgnoreCase(keyword)) return op;
        }
        return null;
    }
}

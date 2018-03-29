package com.lifeform.main.transactions.scripting;

import engine.operators.reserved_set.NoOp;
import engine.operators.reserved_set.Term;

public enum Opcodes {
    TERM(Term.BASE_CODE, Term.KEYWORD),
    NOOP(NoOp.BASE_CODE, NoOp.KEYWORD),
    LMEMSTACK(50, "LMSK"),
    LPCMEMSTACK(51, "LPCMSK"),
    VSIGVAL(52, "VSV"),
    VSIGVALS(53, "VSVS"),
    PUSHAA0(54, "PAA0"),
    PPPUSH(55, "PPP"),
    BRANCH(56, "BRN"),
    GTHAN(57, "GTN"),
    LCONSTSTACK(58, "LCSK"),
    PUSHI0(59, "PI0"),
    CSTACK(60, "CSK"),
    SINT(61, "SI"),
    PUSHI1(62, "PI1"),
    TIME(63, "TIM"),
    DUP(64, "DUP"),
    LHP1PCCSCK(65, "LHP1PCCS"),
    LHPCMEMSCK(66, "LHMPCS"),
    PUSHI255(67, "PI255"),
    PUSHI254(68, "PI254"),
    LOAMEMSCK(69, "LOAMS"),
    LPCCONSTSCK(70, "LPCCS"),
    VADDSAME(71, "VAS"),
    BRN0(72, "BRN0"),
    LOAMTMEMSCK(73, "LOATMS"),
    VRAT(74, "VRAT"),
    SEQ(75, "SEQ"),
    LOTS(76, "LOTS"),
    PUSHI2(77, "PI2"),
    POUTSIZSCK(78, "POSS"),
    POUTSCK(79, "POS"),
    IEQ(80, "IEQ"),
    PUSHI3(81, "PI3"),
    LOATSCK(82, "LOATS"),
    LOAMTTSCK(83, "LOATTS"),
    LOTTS(84, "LOTTS"),
    PUSHI4(85, "PI4"),
    VERNETLOSS(86, "VNL"),
    PUSHI5(87, "PI5"),
    BRANCH1(88, "BRN1"),
    BRANCH2(89, "BRN2"),
    INEQ(90, "INEQ"),
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

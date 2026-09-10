package com.lldcoach.attempt;

public class Submission {
    private String design;
    private String code;
    private String explanation;

    public Submission() {
    }

    public Submission(String design, String code, String explanation) {
        this.design = design;
        this.code = code;
        this.explanation = explanation;
    }

    public String getDesign() {
        return design;
    }

    public void setDesign(String design) {
        this.design = design;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}

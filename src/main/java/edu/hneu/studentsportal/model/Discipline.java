package edu.hneu.studentsportal.model;

import javafx.util.Pair;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.hneu.studentsportal.model.type.DisciplineType;

@Document(collection = "Discipline")
public class Discipline {

    @Id
    private String id;
    private String label;
    private String credits;
    private String controlForm;
    private String mark;
    private DisciplineType type;
    private Pair<Integer, Integer> markPosition;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(final String credits) {
        this.credits = credits;
    }

    public String getControlForm() {
        return controlForm;
    }

    public void setControlForm(final String controlForm) {
        this.controlForm = controlForm;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(final String mark) {
        this.mark = mark;
    }

    public DisciplineType getType() {
        return type;
    }

    public void setType(final DisciplineType type) {
        this.type = type;
    }

    public Pair<Integer, Integer> getMarkPosition() {
        return markPosition;
    }

    public void setMarkPosition(final Pair<Integer, Integer> markPosition) {
        this.markPosition = markPosition;
    }
}

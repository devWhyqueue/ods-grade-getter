package de.devwhyqueue.odsgradegetter.tordownloader.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TranscriptOfRecords implements Serializable {
    public static final int TOTAL_CREDITS = 165;

    private final Set<Module> modules;
    private double gradePointAvg;
    private double bestPossibleGradePointAvg;

    public TranscriptOfRecords() {
        this.modules = new HashSet<>();
    }

    public void addModule(Module module) {
        this.modules.add(module);
        calcGradePointAvgs();
    }

    private void calcGradePointAvgs() {
        double wheightedGradeSum = 0;
        double wheightSum = 0;
        for (Module m : this.modules) {
            wheightedGradeSum += m.getCredits() * m.getGrade();
            wheightSum += m.getCredits();
        }
        this.gradePointAvg = wheightedGradeSum / wheightSum;

        wheightedGradeSum += TOTAL_CREDITS - wheightSum;
        wheightSum += TOTAL_CREDITS - wheightSum;
        this.bestPossibleGradePointAvg = wheightedGradeSum / wheightSum;
    }

    public List<Module> getModules() {
        return this.modules.stream().sorted((Comparator<Module> & Serializable) (a, b) -> b.getDate().compareTo(a.getDate())).collect(Collectors.toList());
    }

    public double getGradePointAvg() {
        return gradePointAvg;
    }

    public double getBestPossibleGradePointAvg() {
        return bestPossibleGradePointAvg;
    }
}

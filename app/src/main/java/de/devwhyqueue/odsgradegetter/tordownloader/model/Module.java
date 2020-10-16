package de.devwhyqueue.odsgradegetter.tordownloader.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Module implements Serializable {
    private final String name;
    private final double grade;
    private final double credits;
    private final LocalDate date;

    public Module(String name, double grade, double credits, LocalDate date) {
        this.name = name;
        this.grade = grade;
        this.credits = credits;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public double getGrade() {
        return grade;
    }

    public double getCredits() {
        return credits;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return name.equals(module.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", grade=" + grade +
                ", credits=" + credits +
                ", date=" + date +
                '}';
    }
}

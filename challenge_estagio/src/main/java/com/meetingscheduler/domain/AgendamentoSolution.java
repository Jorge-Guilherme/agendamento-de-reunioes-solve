package com.meetingscheduler.domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.time.LocalDateTime;
import java.util.List;

@PlanningSolution
public class AgendamentoSolution {
    
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "salaRange")
    private List<Sala> salas;
    
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeSlotRange")
    private List<LocalDateTime> timeSlots;
    
    @PlanningEntityCollectionProperty
    private List<Reuniao> reunioes;
    
    @PlanningScore
    private HardSoftScore score;

    public AgendamentoSolution() {}

    public AgendamentoSolution(List<Sala> salas, List<LocalDateTime> timeSlots, List<Reuniao> reunioes) {
        this.salas = salas;
        this.timeSlots = timeSlots;
        this.reunioes = reunioes;
    }

    public List<Sala> getSalas() {
        return salas;
    }

    public void setSalas(List<Sala> salas) {
        this.salas = salas;
    }

    public List<LocalDateTime> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<LocalDateTime> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public List<Reuniao> getReunioes() {
        return reunioes;
    }

    public void setReunioes(List<Reuniao> reunioes) {
        this.reunioes = reunioes;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public boolean isFeasible() {
        return score != null && score.isFeasible();
    }

    @Override
    public String toString() {
        return "AgendamentoSolution{" +
                "salas=" + salas.size() +
                ", timeSlots=" + timeSlots.size() +
                ", reunioes=" + reunioes.size() +
                ", score=" + score +
                '}';
    }
} 
package com.meetingscheduler.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

import java.time.LocalDateTime;
import java.util.List;

@PlanningEntity
public class Reuniao {
    @PlanningId
    private String id;
    private String titulo;
    private int duracaoMinutos;
    private List<Participante> obrigatorios;
    private List<Participante> preferenciais;
    
    // Planning variables
    private Sala sala;
    private LocalDateTime inicio;

    public Reuniao() {}

    public Reuniao(String titulo, int duracaoMinutos, List<Participante> obrigatorios, List<Participante> preferenciais) {
        this.id = titulo;
        this.titulo = titulo;
        this.duracaoMinutos = duracaoMinutos;
        this.obrigatorios = obrigatorios;
        this.preferenciais = preferenciais;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() { 
        return titulo; 
    }
    
    public void setTitulo(String titulo) { 
        this.titulo = titulo; 
    }

    public int getDuracaoMinutos() { 
        return duracaoMinutos; 
    }
    
    public void setDuracaoMinutos(int duracaoMinutos) { 
        this.duracaoMinutos = duracaoMinutos; 
    }

    public List<Participante> getObrigatorios() { 
        return obrigatorios; 
    }
    
    public void setObrigatorios(List<Participante> obrigatorios) { 
        this.obrigatorios = obrigatorios; 
    }

    public List<Participante> getPreferenciais() { 
        return preferenciais; 
    }
    
    public void setPreferenciais(List<Participante> preferenciais) { 
        this.preferenciais = preferenciais; 
    }

    @PlanningVariable(valueRangeProviderRefs = {"salaRange"})
    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    @PlanningVariable(valueRangeProviderRefs = {"timeSlotRange"})
    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        if (inicio == null) {
            return null;
        }
        return inicio.plusMinutes(duracaoMinutos);
    }

    public int getTotalParticipantes() {
        int total = 0;
        if (obrigatorios != null) {
            total += obrigatorios.size();
        }
        if (preferenciais != null) {
            total += preferenciais.size();
        }
        return total;
    }

    @Override
    public String toString() {
        return "Reuniao{" +
                "titulo='" + titulo + '\'' +
                ", duracao=" + duracaoMinutos + "min" +
                ", sala=" + (sala != null ? sala.getNome() : "não alocada") +
                ", inicio=" + inicio +
                '}';
    }
} 
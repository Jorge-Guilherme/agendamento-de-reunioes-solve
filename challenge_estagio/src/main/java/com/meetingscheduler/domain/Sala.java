package com.meetingscheduler.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Sala {
    private String nome;
    private int capacidade;

    public Sala() {}

    public Sala(String nome, int capacidade) {
        this.nome = nome;
        this.capacidade = capacidade;
    }

    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public int getCapacidade() { 
        return capacidade; 
    }
    
    public void setCapacidade(int capacidade) { 
        this.capacidade = capacidade; 
    }

    @Override
    public String toString() {
        return "Sala{" +
                "nome='" + nome + '\'' +
                ", capacidade=" + capacidade +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sala sala = (Sala) o;
        return nome.equals(sala.nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
} 
package com.meetingscheduler.solver;

import com.meetingscheduler.domain.AgendamentoSolution;
import com.meetingscheduler.domain.Participante;
import com.meetingscheduler.domain.Reuniao;
import com.meetingscheduler.domain.Sala;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class AgendamentoConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // Hard Constraints
                conflitoSala(constraintFactory),
                participacaoObrigatoria(constraintFactory),
                capacidadeMinimaSala(constraintFactory),
                inicioTérminoMesmoDia(constraintFactory),
                
                // Medium Constraints
                participacaoPreferencial(constraintFactory),
                
                // Soft Constraints
                agendarCedo(constraintFactory),
                intervaloEntreReunioes(constraintFactory),
                minimizarReunioesSimultaneas(constraintFactory),
                alocarSalasMaiores(constraintFactory),
                estabilidadeSala(constraintFactory)
        };
    }

    // HARD CONSTRAINTS

    /**
     * Conflito de sala: Duas reuniões não devem utilizar a mesma sala ao mesmo tempo.
     */
    private Constraint conflitoSala(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Reuniao.class,
                        Joiners.equal(Reuniao::getSala),
                        Joiners.overlapping(Reuniao::getInicio, Reuniao::getFim))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Conflito de sala");
    }

    /**
     * Participação obrigatória: Uma pessoa não pode ter duas reuniões obrigatórias ao mesmo tempo.
     */
    private Constraint participacaoObrigatoria(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Reuniao.class,
                        Joiners.overlapping(Reuniao::getInicio, Reuniao::getFim))
                .filter((reuniao1, reuniao2) -> 
                    hasCommonParticipant(reuniao1.getObrigatorios(), reuniao2.getObrigatorios()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Participação obrigatória conflitante");
    }

    /**
     * Capacidade mínima da sala: Uma reunião não pode ser alocada em uma sala que não comporte todos os seus participantes.
     */
    private Constraint capacidadeMinimaSala(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Reuniao.class)
                .filter(reuniao -> reuniao.getSala() != null && 
                        reuniao.getTotalParticipantes() > reuniao.getSala().getCapacidade())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Capacidade mínima da sala");
    }

    /**
     * Início e término no mesmo dia: Uma reunião não deve ser agendada atravessando mais de um dia.
     */
    private Constraint inicioTérminoMesmoDia(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Reuniao.class)
                .filter(reuniao -> reuniao.getInicio() != null && 
                        reuniao.getFim() != null &&
                        !reuniao.getInicio().toLocalDate().equals(reuniao.getFim().toLocalDate()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Início e término em dias diferentes");
    }

    // MEDIUM CONSTRAINTS

    /**
     * Participação preferencial: Uma pessoa não pode ter duas reuniões preferenciais ao mesmo tempo, 
     * nem uma reunião obrigatória e uma preferencial no mesmo horário.
     */
    private Constraint participacaoPreferencial(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Reuniao.class,
                        Joiners.overlapping(Reuniao::getInicio, Reuniao::getFim))
                .filter((reuniao1, reuniao2) -> 
                    hasCommonParticipant(reuniao1.getPreferenciais(), reuniao2.getPreferenciais()) ||
                    hasCommonParticipant(reuniao1.getObrigatorios(), reuniao2.getPreferenciais()) ||
                    hasCommonParticipant(reuniao1.getPreferenciais(), reuniao2.getObrigatorios()))
                .penalize(HardSoftScore.of(0, 10))
                .asConstraint("Participação preferencial conflitante");
    }

    // SOFT CONSTRAINTS

    /**
     * Quanto antes, melhor: Agendar todas as reuniões o mais cedo possível.
     */
    private Constraint agendarCedo(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Reuniao.class)
                .filter(reuniao -> reuniao.getInicio() != null)
                .reward(HardSoftScore.of(0, 1))
                .asConstraint("Agendar cedo");
    }

    /**
     * Intervalo entre reuniões: Duas reuniões devem ter pelo menos um intervalo de tempo entre elas.
     */
    private Constraint intervaloEntreReunioes(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Reuniao.class,
                        Joiners.equal(Reuniao::getSala))
                .filter((reuniao1, reuniao2) -> {
                    if (reuniao1.getInicio() == null || reuniao2.getInicio() == null) return false;
                    Duration duration = Duration.between(reuniao1.getFim(), reuniao2.getInicio());
                    return duration.isNegative() || duration.toMinutes() < 15; // Mínimo 15 minutos
                })
                .penalize(HardSoftScore.of(0, 5))
                .asConstraint("Intervalo entre reuniões");
    }

    /**
     * Reuniões simultâneas: Minimizar o número de reuniões paralelas.
     */
    private Constraint minimizarReunioesSimultaneas(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Reuniao.class,
                        Joiners.overlapping(Reuniao::getInicio, Reuniao::getFim))
                .penalize(HardSoftScore.of(0, 3))
                .asConstraint("Reuniões simultâneas");
    }

    /**
     * Alocar salas maiores primeiro: Se houver uma sala maior disponível, a reunião deve ser alocada nela.
     */
    private Constraint alocarSalasMaiores(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Reuniao.class)
                .filter(reuniao -> reuniao.getSala() != null)
                .reward(HardSoftScore.ONE_SOFT, reuniao -> reuniao.getSala().getCapacidade())
                .asConstraint("Alocar salas maiores");
    }

    /**
     * Estabilidade de sala: Se uma pessoa tiver duas reuniões consecutivas com até dois intervalos de tempo entre elas, 
     * é preferível que ambas ocorram na mesma sala.
     */
    private Constraint estabilidadeSala(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Reuniao.class)
                .filter((reuniao1, reuniao2) -> {
                    if (reuniao1.getSala() == null || reuniao2.getSala() == null) return false;
                    if (reuniao1.getSala().equals(reuniao2.getSala())) return false;
                    
                    // Verificar se há participantes em comum
                    if (!hasCommonParticipant(reuniao1.getObrigatorios(), reuniao2.getObrigatorios()) &&
                        !hasCommonParticipant(reuniao1.getPreferenciais(), reuniao2.getPreferenciais())) {
                        return false;
                    }
                    
                    // Verificar se são consecutivas (até 2 intervalos de tempo)
                    if (reuniao1.getInicio() == null || reuniao2.getInicio() == null) return false;
                    Duration duration = Duration.between(reuniao1.getFim(), reuniao2.getInicio());
                    return duration.toMinutes() <= 30; // 2 intervalos de 15 minutos
                })
                .penalize(HardSoftScore.of(0, 2))
                .asConstraint("Estabilidade de sala");
    }

    // Helper methods

    private boolean hasCommonParticipant(List<Participante> lista1, List<Participante> lista2) {
        if (lista1 == null || lista2 == null) return false;
        return lista1.stream().anyMatch(p1 -> 
            lista2.stream().anyMatch(p2 -> p1.getNome().equals(p2.getNome())));
    }
} 
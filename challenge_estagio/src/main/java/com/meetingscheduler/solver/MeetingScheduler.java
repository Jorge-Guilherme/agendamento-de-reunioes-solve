package com.meetingscheduler.solver;

import com.meetingscheduler.domain.AgendamentoSolution;
import com.meetingscheduler.domain.Reuniao;
import com.meetingscheduler.domain.Sala;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class MeetingScheduler {
    
    private final Solver<AgendamentoSolution> solver;
    
    public MeetingScheduler() {
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(AgendamentoSolution.class)
                .withEntityClasses(Reuniao.class)
                .withConstraintProviderClass(AgendamentoConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig()
                        .withSpentLimit(Duration.ofMinutes(5))
                        .withUnimprovedSpentLimit(Duration.ofMinutes(2)));
        
        SolverFactory<AgendamentoSolution> solverFactory = SolverFactory.create(solverConfig);
        this.solver = solverFactory.buildSolver();
    }
    
    public AgendamentoSolution solve(List<Sala> salas, List<Reuniao> reunioes, List<LocalDateTime> timeSlots) {
        AgendamentoSolution problem = new AgendamentoSolution(salas, timeSlots, reunioes);
        
        System.out.println("Iniciando solução do problema de agendamento...");
        System.out.println("Salas: " + salas.size());
        System.out.println("Reuniões: " + reunioes.size());
        System.out.println("Time slots: " + timeSlots.size());
        
        AgendamentoSolution solution = solver.solve(problem);
        
        System.out.println("Solução encontrada!");
        System.out.println("Score: " + solution.getScore());
        System.out.println("É viável: " + solution.isFeasible());
        
        return solution;
    }
    
    public void printSolution(AgendamentoSolution solution) {
        System.out.println("\n=== SOLUÇÃO DE AGENDAMENTO ===");
        System.out.println("Score: " + solution.getScore());
        System.out.println("É viável: " + solution.isFeasible());
        
        List<Reuniao> reunioes = solution.getReunioes();
        reunioes.sort((r1, r2) -> {
            if (r1.getInicio() == null && r2.getInicio() == null) return 0;
            if (r1.getInicio() == null) return 1;
            if (r2.getInicio() == null) return -1;
            return r1.getInicio().compareTo(r2.getInicio());
        });
        
        System.out.println("\nReuniões agendadas:");
        System.out.println("Título | Duração | Sala | Início | Fim | Participantes");
        System.out.println("-------|---------|------|--------|-----|--------------");
        
        for (Reuniao reuniao : reunioes) {
            if (reuniao.getInicio() != null && reuniao.getSala() != null) {
                System.out.printf("%-30s | %3d min | %-4s | %s | %s | %d part.\n",
                        reuniao.getTitulo().substring(0, Math.min(30, reuniao.getTitulo().length())),
                        reuniao.getDuracaoMinutos(),
                        reuniao.getSala().getNome(),
                        formatDateTime(reuniao.getInicio()),
                        formatDateTime(reuniao.getFim()),
                        reuniao.getTotalParticipantes());
            } else {
                System.out.printf("%-30s | %3d min | %-4s | %s | %s | %d part.\n",
                        reuniao.getTitulo().substring(0, Math.min(30, reuniao.getTitulo().length())),
                        reuniao.getDuracaoMinutos(),
                        "N/A",
                        "N/A",
                        "N/A",
                        reuniao.getTotalParticipantes());
            }
        }
        
        // Estatísticas
        long reunioesAgendadas = reunioes.stream()
                .filter(r -> r.getInicio() != null && r.getSala() != null)
                .count();
        
        System.out.println("\n=== ESTATÍSTICAS ===");
        System.out.println("Total de reuniões: " + reunioes.size());
        System.out.println("Reuniões agendadas: " + reunioesAgendadas);
        System.out.println("Reuniões não agendadas: " + (reunioes.size() - reunioesAgendadas));
        System.out.println("Taxa de sucesso: " + String.format("%.1f%%", (double) reunioesAgendadas / reunioes.size() * 100));
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }
    
    public void close() {
        if (solver != null) {
            solver.terminateEarly();
        }
    }
} 
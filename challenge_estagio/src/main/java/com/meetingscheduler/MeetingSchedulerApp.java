package com.meetingscheduler;

import com.meetingscheduler.data.ExcelDataReader;
import com.meetingscheduler.solver.MeetingScheduler;
import com.meetingscheduler.domain.AgendamentoSolution;

import java.io.IOException;

public class MeetingSchedulerApp {
    
    public static void main(String[] args) {
        String filePath = "salas_50_reunioes.xlsx";
        
        if (args.length > 0) {
            filePath = args[0];
        }
        
        System.out.println("=== SOLVER DE AGENDAMENTO DE REUNIÕES ===");
        System.out.println("Arquivo: " + filePath);
        System.out.println();
        
        try {
            // Ler dados do Excel
            System.out.println("Lendo dados do arquivo Excel...");
            ExcelDataReader.MeetingData data = ExcelDataReader.readFromExcel(filePath);
            
            System.out.println("Dados carregados:");
            System.out.println("- Salas: " + data.getSalas().size());
            System.out.println("- Reuniões: " + data.getReunioes().size());
            System.out.println("- Time slots: " + data.getTimeSlots().size());
            System.out.println();
            
            // Mostrar salas disponíveis
            System.out.println("Salas disponíveis:");
            for (var sala : data.getSalas()) {
                System.out.printf("- %s (capacidade: %d)\n", sala.getNome(), sala.getCapacidade());
            }
            System.out.println();
            
            // Mostrar algumas reuniões
            System.out.println("Primeiras 5 reuniões:");
            for (int i = 0; i < Math.min(5, data.getReunioes().size()); i++) {
                var reuniao = data.getReunioes().get(i);
                System.out.printf("- %s (%d min, %d participantes)\n", 
                    reuniao.getTitulo(), 
                    reuniao.getDuracaoMinutos(),
                    reuniao.getTotalParticipantes());
            }
            System.out.println();
            
            // Executar o solver
            MeetingScheduler scheduler = new MeetingScheduler();
            AgendamentoSolution solution = scheduler.solve(
                data.getSalas(), 
                data.getReunioes(), 
                data.getTimeSlots()
            );
            
            // Mostrar resultados
            scheduler.printSolution(solution);
            
            scheduler.close();
            
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo Excel: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
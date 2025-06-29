package com.meetingscheduler.data;

import com.meetingscheduler.domain.Participante;
import com.meetingscheduler.domain.Reuniao;
import com.meetingscheduler.domain.Sala;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelDataReader {
    
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})-(\\d{1,2}):(\\d{2})");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\w+)\\s+(\\d{1,2})/(\\d{1,2})/(\\d{4})");
    
    public static class MeetingData {
        private final List<Sala> salas;
        private final List<Reuniao> reunioes;
        private final List<LocalDateTime> timeSlots;
        
        public MeetingData(List<Sala> salas, List<Reuniao> reunioes, List<LocalDateTime> timeSlots) {
            this.salas = salas;
            this.reunioes = reunioes;
            this.timeSlots = timeSlots;
        }
        
        public List<Sala> getSalas() { return salas; }
        public List<Reuniao> getReunioes() { return reunioes; }
        public List<LocalDateTime> getTimeSlots() { return timeSlots; }
    }
    
    public static MeetingData readFromExcel(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Ler salas (primeiras linhas)
            List<Sala> salas = readSalas(sheet);
            
            // Ler reuniões
            List<Reuniao> reunioes = readReunioes(sheet);
            
            // Gerar time slots
            List<LocalDateTime> timeSlots = generateTimeSlots(sheet);
            
            return new MeetingData(salas, reunioes, timeSlots);
        }
    }
    
    private static List<Sala> readSalas(Sheet sheet) {
        List<Sala> salas = new ArrayList<>();
        
        // Procurar pelas salas nas primeiras linhas
        for (int rowNum = 0; rowNum < 20; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            
            // Verificar coluna B (índice 1) para nome da sala
            Cell salaCell = row.getCell(1);
            if (salaCell != null && salaCell.getCellType() == CellType.STRING) {
                String salaValue = salaCell.getStringCellValue();
                if (salaValue != null && salaValue.startsWith("S ") && salaValue.length() > 2) {
                    // Encontrar a capacidade na coluna C (índice 2)
                    Cell capacityCell = row.getCell(2);
                    if (capacityCell != null && capacityCell.getCellType() == CellType.NUMERIC) {
                        int capacidade = (int) capacityCell.getNumericCellValue();
                        salas.add(new Sala(salaValue, capacidade));
                        System.out.println("Sala encontrada: " + salaValue + " (capacidade: " + capacidade + ")");
                    }
                }
            }
        }
        
        // Se não encontrou salas, criar algumas salas padrão
        if (salas.isEmpty()) {
            System.out.println("Nenhuma sala encontrada no Excel. Criando salas padrão...");
            salas.add(new Sala("S 1", 30));
            salas.add(new Sala("S 2", 20));
            salas.add(new Sala("S 3", 16));
            salas.add(new Sala("S 4", 14));
            salas.add(new Sala("S 5", 12));
        }
        
        return salas;
    }
    
    private static List<Reuniao> readReunioes(Sheet sheet) {
        List<Reuniao> reunioes = new ArrayList<>();
        
        // Procurar pela linha de cabeçalho das reuniões
        int headerRow = -1;
        for (int rowNum = 0; rowNum < 20; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            
            Cell cell = row.getCell(0);
            if (cell != null && cell.getCellType() == CellType.STRING &&
                cell.getStringCellValue().contains("Reuniões a Serem Agendadas")) {
                headerRow = rowNum;
                break;
            }
        }
        
        if (headerRow == -1) {
            throw new RuntimeException("Não foi possível encontrar o cabeçalho das reuniões");
        }
        
        // Ler as reuniões a partir da linha de cabeçalho + 1
        for (int rowNum = headerRow + 1; rowNum < sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            
            Cell topicCell = row.getCell(1); // Coluna B - Topic
            Cell durationCell = row.getCell(2); // Coluna C - Duration
            
            if (topicCell != null && topicCell.getCellType() == CellType.STRING &&
                durationCell != null && durationCell.getCellType() == CellType.NUMERIC) {
                
                String titulo = topicCell.getStringCellValue().trim();
                int duracao = (int) durationCell.getNumericCellValue();
                
                if (!titulo.isEmpty() && duracao > 0) {
                    // Para simplificar, vamos criar participantes fictícios
                    // Em um cenário real, estes dados viriam do Excel
                    List<Participante> obrigatorios = generateParticipantes(titulo, "obrigatorio", 2);
                    List<Participante> preferenciais = generateParticipantes(titulo, "preferencial", 1);
                    
                    reunioes.add(new Reuniao(titulo, duracao, obrigatorios, preferenciais));
                }
            }
        }
        
        return reunioes;
    }
    
    private static List<LocalDateTime> generateTimeSlots(Sheet sheet) {
        List<LocalDateTime> timeSlots = new ArrayList<>();
        
        // Procurar pelas datas nas primeiras linhas
        LocalDate startDate = null;
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(18, 0);
        
        for (int rowNum = 0; rowNum < 10; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            
            Cell cell = row.getCell(1);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue();
                Matcher matcher = DATE_PATTERN.matcher(cellValue);
                if (matcher.find()) {
                    // Encontrar a primeira data para usar como referência
                    int day = Integer.parseInt(matcher.group(2));
                    int month = Integer.parseInt(matcher.group(3));
                    int year = Integer.parseInt(matcher.group(4));
                    startDate = LocalDate.of(year, month, day);
                    break;
                }
            }
        }
        
        if (startDate == null) {
            // Usar uma data padrão se não encontrar
            startDate = LocalDate.of(2025, 6, 30);
        }
        
        // Gerar time slots para 5 dias úteis, de 15 em 15 minutos
        for (int day = 0; day < 5; day++) {
            LocalDate currentDate = startDate.plusDays(day);
            
            LocalDateTime currentTime = LocalDateTime.of(currentDate, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(currentDate, endTime);
            
            while (currentTime.isBefore(endDateTime)) {
                timeSlots.add(currentTime);
                currentTime = currentTime.plusMinutes(15);
            }
        }
        
        return timeSlots;
    }
    
    private static List<Participante> generateParticipantes(String reuniaoTitulo, String tipo, int count) {
        List<Participante> participantes = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            participantes.add(new Participante("P" + tipo.charAt(0) + "_" + reuniaoTitulo.substring(0, Math.min(3, reuniaoTitulo.length())) + "_" + i));
        }
        return participantes;
    }
} 
# Solver de Agendamento de Reuniões

Este projeto implementa um solver para agendamento de reuniões usando OptaPlanner, um framework de otimização de código aberto. A ideia é resolver o desafio para concorrer a uma vaga de estágio.

## Descrição do Problema

O solver resolve o problema de agendamento de reuniões considerando as seguintes restrições:

### Restrições Rígidas (Hard Constraints)

- **Conflito de sala**: Duas reuniões não devem utilizar a mesma sala ao mesmo tempo
- **Participação obrigatória**: Uma pessoa não pode ter duas reuniões obrigatórias ao mesmo tempo
- **Capacidade mínima da sala**: Uma reunião não pode ser alocada em uma sala que não comporte todos os seus participantes
- **Início e término no mesmo dia**: Uma reunião não deve ser agendada atravessando mais de um dia

### Restrições Médias (Medium Constraints)

- **Participação preferencial**: Uma pessoa não pode ter duas reuniões preferenciais ao mesmo tempo, nem uma reunião obrigatória e uma preferencial no mesmo horário

### Restrições Leves (Soft Constraints)

- **Quanto antes, melhor**: Agendar todas as reuniões o mais cedo possível
- **Intervalo entre reuniões**: Duas reuniões devem ter pelo menos um intervalo de tempo entre elas
- **Reuniões simultâneas**: Minimizar o número de reuniões paralelas
- **Alocar salas maiores primeiro**: Se houver uma sala maior disponível, a reunião deve ser alocada nela
- **Estabilidade de sala**: Se uma pessoa tiver duas reuniões consecutivas, é preferível que ambas ocorram na mesma sala

## Tecnologias Utilizadas

- **Java 11** - Linguagem de Programação
- **OptaPlanner 9.44.0.Final** - Framework de otimização
- **Apache POI 5.2.3** - Leitura de arquivos Excel
- **Maven** - Gerenciamento de dependências
- **Logback** - Sistema de logs

## Estrutura do Projeto

```
src/main/java/com/meetingscheduler/
├── domain/                    # Classes de domínio
│   ├── Sala.java             # Entidade Sala
│   ├── Participante.java     # Entidade Participante
│   ├── Reuniao.java          # Entidade Reunião (Planning Entity)
│   └── AgendamentoSolution.java # Classe principal da solução
├── solver/                   # Lógica do solver
│   ├── AgendamentoConstraintProvider.java # Restrições
│   └── MeetingScheduler.java # Configuração e execução do solver
├── data/                     # Leitura de dados
│   └── ExcelDataReader.java  # Leitor de arquivos Excel
└── MeetingSchedulerApp.java  # Aplicação principal
```

## Como Executar

### Pré-requisitos

- Java 11 ou superior
- Maven 3.6 ou superior

### Compilação

```bash
mvn clean compile
```

### Execução

#### Execução Básica

```bash
# Executar padrão (salas_50_reunioes.xlsx -> defini como arquivo padrão)
mvn exec:java -Dexec.mainClass="com.meetingscheduler.MeetingSchedulerApp"
```

#### Execução com Outro Arquivo

```bash
# Executar com arquivo de 200 reuniões por exemplo
mvn exec:java -Dexec.mainClass="com.meetingscheduler.MeetingSchedulerApp" -Dexec.args="salas_200_reunioes.xlsx"
```

## Formato dos Dados

O solver espera arquivos Excel (.xlsx) com a seguinte estrutura:

### Salas

- Coluna B: Nome da sala (ex: "S 1", "S 2")
- Coluna C: Capacidade da sala

### Reuniões

- Coluna B: Título da reunião
- Coluna C: Duração em minutos

### Datas e Horários

- O solver gera automaticamente time slots de 15 em 15 minutos
- Horário de funcionamento: 8:00 às 18:00
- Período: 5 dias úteis

## Configuração do Solver

O solver está configurado com:

- **Tempo limite**: 5 minutos
- **Tempo sem melhoria**: 2 minutos
- **Algoritmo**: OptaPlanner escolhe automaticamente o melhor algoritmo

## Saída do Programa

### Informações Exibidas

1. **Dados carregados**: Número de salas, reuniões e time slots
2. **Salas disponíveis**: Lista com nome e capacidade
3. **Reuniões**: Primeiras 5 reuniões com detalhes
4. **Processo de otimização**: Logs detalhados do OptaPlanner
5. **Solução**: Tabela com todas as reuniões agendadas
6. **Estatísticas**: Taxa de sucesso do agendamento

### Exemplo de Saída

```
=== SOLVER DE AGENDAMENTO DE REUNIÕES ===
Arquivo: salas_50_reunioes.xlsx

Dados carregados:
- Salas: 5
- Reuniões: 49
- Time slots: 200

Salas disponíveis:
- S 1 (capacidade: 30)
- S 2 (capacidade: 20)
- S 3 (capacidade: 16)
- S 4 (capacidade: 14)
- S 5 (capacidade: 12)

=== SOLUÇÃO DE AGENDAMENTO ===
Score: 0hard/1338soft
É viável: true

Reuniões agendadas:
Título                         | Duração | Sala | Início | Fim   | Participantes
Strategize B2B                 |  45 min | S 1  | 30/06 08:00 | 30/06 08:45 | 3 part.
Fast track e-business          | 120 min | S 2  | 30/06 09:00 | 30/06 11:00 | 3 part.
...

=== ESTATÍSTICAS ===
Total de reuniões: 49
Reuniões agendadas: 49
Reuniões não agendadas: 0
Taxa de sucesso: 100,0%
```

## Resultados Obtidos

### Teste com 50 Reuniões

- ✅ **Taxa de sucesso**: 100% (49/49 reuniões agendadas)
- ✅ **Score**: 0hard/1338soft (solução viável)
- ⏱️ **Tempo de execução**: ~5 minutos
- 🏢 **Utilização de salas**: Distribuição inteligente por capacidade

### Performance

### Complexidade Algorítmica

O solver utiliza uma combinação de algoritmos de otimização com diferentes complexidades:

#### **1. Construction Heuristic (Fase Inicial)**

- **Algoritmo**: First Fit Decreasing
- **Complexidade**: O(n × m × k)
  - n = número de reuniões
  - m = número de salas
  - k = número de time slots
- **Tempo observado**: ~1.5 segundos para 49 reuniões
- **Objetivo**: Gerar uma solução inicial viável rapidamente

#### **2. Local Search (Fase de Otimização)**

- **Algoritmo**: Tabu Search + Simulated Annealing
- **Complexidade**: O(i × n² × m)
  - i = número de iterações (76.852 no teste com 49 reuniões)
  - n = número de reuniões
  - m = número de salas
- **Tempo observado**: ~5 minutos para 49 reuniões
- **Objetivo**: Melhorar a solução através de movimentos locais

#### **3. Constraint Evaluation**

- **Complexidade**: O(n²) para cada avaliação
- **Velocidade**: ~10.000 avaliações/segundo
- **Objetivo**: Calcular o score da solução atual

### Escalabilidade

O tempo de execução cresce de forma não-linear:

- **49 reuniões**: ~5 minutos
- **200 reuniões**: ~20-30 minutos (estimado)
- **500 reuniões**: ~2-3 horas (estimado)

### Otimizações Implementadas

1. **Constraint Streams**: Avaliação eficiente de restrições
2. **Incremental Score Calculation**: Recalcula apenas o que mudou
3. **Early Termination**: Para quando não há melhoria por 2 minutos
4. **Parallel Processing**: Suporte a múltiplas threads (configurável)

O solver utiliza algoritmos de otimização avançados:

- **Construction Heuristics**: Para gerar uma solução inicial viável
- **Local Search**: Para melhorar a solução
- **Metaheuristics**: Para encontrar soluções ótimas

### 1. Execução com Logs Completos

```bash
mvn exec:java -Dexec.mainClass="com.meetingscheduler.MeetingSchedulerApp" | tee output_detalhado.txt
```

### 2. Visualizar Arquivo de Saída

```bash
cat output_detalhado.txt
less output_detalhado.txt
open output_detalhado.txt
```

### Logs de Debug

Para ver logs mais detalhados, modifique `src/main/resources/logback.xml`:

```xml
<logger name="com.meetingscheduler" level="DEBUG"/>
```

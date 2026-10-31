# API Inteligente de Orçamento — Spring Boot + Spring AI

Projeto do Desafio de Projeto da DIO, evoluído a partir do módulo `05-spring-ai` da trilha
[dio-spring-boot-learning-track](https://github.com/digitalinnovationone/dio-spring-boot-learning-track).

## O que o projeto faz

É uma API de orçamento que entende **comandos de voz** sobre transações financeiras.

```
áudio (.m4a) ──► Whisper (texto) ──► ChatClient + Tool Calling ──► caso de uso ──► banco (MySQL)
                                                                        │
resposta em áudio (.mp3) ◄── Text-to-Speech ◄── resposta em texto ◄─────┘
```

1. O cliente envia um arquivo de áudio para `POST /transactions/ai`.
2. O áudio é transcrito para texto (`TranscriptionModel`).
3. O modelo de linguagem entende a intenção e escolhe uma **tool** da aplicação.
4. O caso de uso registra ou consulta transações.
5. A resposta final é convertida em áudio (`TextToSpeechModel`).

Os mesmos casos de uso atendem o REST e o Tool Calling, e a arquitetura segue em camadas
(`domain` / `application` / `infrastructure`).

## Melhoria implementada

Escolhi uma evolução pequena, mas que atravessa todas as camadas: **total gasto por categoria + validações**.

### 1. Nova tool: total por categoria

- Caso de uso `SumTransactionsByCategoryUseCase`, exposto ao modelo como a tool `sum-transactions-by-category`.
  Agora a IA responde "quanto gastei no mercado?" com uma soma feita no banco, em vez de listar tudo e somar de cabeça.
- Consulta `sum(amount)` no `TransactionEntityRepository`, atrás do contrato `TransactionRepository.sumAmountByCategory`
  (o domínio continua sem depender de JPA).
- Novo endpoint REST: `GET /transactions/{category}/total`.
- O system prompt (`prompts/system-message.st`) orienta o modelo a usar a nova tool e a responder de forma curta, já que a resposta vira áudio.

### 2. Validações antes de salvar

- **Domínio** (`Transaction`): descrição obrigatória e com até 255 caracteres, valor maior que zero e categoria obrigatória.
  Vale para o REST e também para a IA, porque a regra fica na entidade e não no controller.
- **REST** (`TransactionRequest` + `@Valid`): Bean Validation com mensagens em português.
- **Erros** (`ApiExceptionHandler`): `400 Bad Request` no formato `ProblemDetail`, com a lista de campos inválidos.
- **Banco**: coluna `description` agora é `NOT NULL` e tem tamanho 255 explícito.

### 3. Correção de unidade (centavos x reais)

O domínio guarda valores em **centavos** (a tool pede "valor em centavos"), mas a saída devolvia esse número como se fosse reais:
um gasto de R$ 80,00 aparecia como `8000.0`, e a IA falaria "oito mil reais". Agora `TransactionOutput` converte para reais.

> Isso muda o JSON de saída de `POST /transactions` e `GET /transactions/{category}`: `amount` passa a vir em reais.
> A **entrada** continua em centavos.

## Tecnologias

- Java 25, Spring Boot 4.0.5, Gradle 9.4.1
- Spring AI 2.0.0-M4 (OpenAI: `whisper-1`, `gpt-4o-mini`, `gpt-4o-mini-tts`)
- Spring Data JPA + MySQL 9.6 (Docker Compose)
- Bean Validation, JUnit 5, Mockito, AssertJ, MockMvc

## Como executar

Pré-requisitos: JDK 25, Docker em execução e uma chave da OpenAI.

```bash
cd 05-spring-ai
export OPENAI_API_KEY="sua_chave_aqui"     # PowerShell: $env:OPENAI_API_KEY="sua_chave_aqui"
./gradlew bootRun                           # o Spring Boot sobe o MySQL via compose.yml
```

## Como testar

### Testes automatizados (não precisam de OpenAI nem de banco)

```bash
./gradlew test --tests 'dio.budgeting.domain.*' --tests 'dio.budgeting.application.*' --tests 'dio.budgeting.infrastructure.*'
```

| Teste | O que garante |
|---|---|
| `TransactionTest` | descrição vazia/longa, valor ≤ 0 e categoria nula são rejeitados |
| `PersistTransactionUseCaseTest` | input inválido nunca chega ao repositório; saída em reais |
| `SumTransactionsByCategoryUseCaseTest` | total em reais e total 0 para categoria sem transações |
| `TransactionControllerTest` | 201 válido, 400 com erros por campo, endpoint de total, categoria inválida |

Os testes `*IT` do projeto base continuam existindo e só rodam com `OPENAI_API_KEY` definida.

### Fluxo REST

```bash
# Criar uma transação (valor em centavos: R$ 80,00)
curl -X POST localhost:8080/transactions -H "Content-Type: application/json" \
  -d '{"description":"Mercado","category":"GROCERIES","amount":8000}'

# Transação inválida -> 400 com a lista de erros
curl -X POST localhost:8080/transactions -H "Content-Type: application/json" \
  -d '{"description":"","amount":0}'

# Total da categoria (em reais)
curl localhost:8080/transactions/GROCERIES/total
```

Categorias disponíveis: `GROCERIES`, `PHARMA`, `AUTO`.

### Fluxo principal por voz

```bash
curl -X POST localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" --output resposta.mp3
```

Os áudios `recording-1.m4a` a `recording-6.m4a` são gastos ditos em voz. Para testar a nova tool, grave um áudio
curto perguntando "quanto eu gastei no mercado?" e envie da mesma forma.

### Prints e exemplos

<!-- Adicione aqui prints/saídas das requisições acima rodando. -->

## O que aprendi

<!-- Escreva com suas palavras. Sugestões de pontos: -->
<!-- - Como o Tool Calling liga o modelo a um caso de uso real e por que a regra de validação fica no domínio. -->
<!-- - O bug de centavos x reais: a IA só é tão certa quanto os dados que as tools devolvem. -->
<!-- - Testar sem chamar a OpenAI (mocks, MockMvc) e deixar os testes de integração opcionais. -->

## Documentação do Spring AI

- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/index.html)
- [ChatClient API](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Tools API](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [Audio Transcriptions](https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html)
- [Audio Speech](https://docs.spring.io/spring-ai/reference/api/audio/speech.html)

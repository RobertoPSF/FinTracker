# FinTracker

App Android que lê notificações de bancos, classifica transações (entrada/saída) e monta extrato e balanço mensal — 100% local, banco criptografado (SQLCipher).

## Estado

| Fase | Escopo | Status |
|---|---|---|
| 0 | Setup, Room + SQLCipher, estrutura de pacotes | feito |
| 1 | `NotificationListenerService`, permissão, raw persistida | feito |
| 2 | Classificação híbrida, dedup por hash, testes | feito |
| 3 | Entidades, migrações versionadas, agrupamento mensal, fila de revisão | feito |
| 4 | Extrato por banco, balanço, revisão, edição | feito |
| 5 | Empréstimo/cobrança (campos + tela "A cobrar") | feito |
| 6 | Bloqueio biométrico | pendente (dependência já incluída) |
| 7 | Export CSV (`data/repo/Exporter.kt`); import | parcial |
| 8-9 | Categorias, gráficos, F-Droid | pendente |

## Estrutura

```
app/src/main/java/com/rfilh/fintracker/
  core/          Money, Dates, Hashing, TextNormalizer
  parser/        ParserRules, TransactionClassifier, RulesProvider
  notification/  BankNotificationListener, NotificationIngestor, NotificationAccess
  domain/        modelos + interface de repositório
  data/          Room (entidades, DAOs, SQLCipher), repositório
  ui/            Compose (nav, telas, viewmodels)
app/src/main/assets/parser_rules.json   regras de parsing (externas ao código)
```

## Build

1. Abrir a pasta no Android Studio (Ladybug+) e sincronizar. O wrapper Gradle (`gradlew`, `gradle-wrapper.jar`) é gerado pelo Studio; alternativamente: `gradle wrapper --gradle-version 8.9`.
2. `./gradlew :app:testDebugUnitTest` roda os testes do parser.
3. `./gradlew :app:installDebug`.

Requisitos: JDK 17, AGP 8.5.2, compileSdk 34, minSdk 24.

## Uso

1. Abrir o app → botão "Conceder" → Configurações > Acesso a notificações → habilitar FinTracker.
2. Notificações dos apps listados em `parser_rules.json` são capturadas, classificadas e persistidas.
3. Transações com confiança baixa/`UNKNOWN` caem na fila "Revisar".

## Parsing

`TransactionClassifier` é Kotlin puro (sem Android), testável direto:

- keywords → `INCOME` / `EXPENSE` (a que aparece primeiro no texto vence)
- regex → valor (`R$ 1.234,56` → `123456` centavos)
- regex → contraparte (best-effort, com restauração de acentos/caixa)
- `Confidence`: HIGH (banco conhecido + tipo + valor), MEDIUM (sem banco conhecido), LOW (sem valor)
- Sem tipo reconhecido: ignora se bater lista `ignore` ou se não houver valor

Adicionar um banco = adicionar entrada em `parser_rules.json` (`package`, `name`, opcionalmente `ignore`/`income`/`expense`/`amountPatterns`/`counterpartyPatterns`) e um teste em `TransactionClassifierTest`.
`RulesProvider` prioriza `filesDir/parser_rules_override.json`, permitindo atualizar regras sem release.

## Dedup

Índice único em `raw_notification.hash`, com hash = SHA-256 de `pacote|título|corpo|janela de 60s`. Insert com `OnConflictStrategy.IGNORE`; retorno `-1` = duplicata.

## Segurança

- SQLCipher via `SupportOpenHelperFactory`; passphrase de 32 bytes aleatórios em `EncryptedSharedPreferences` (Android Keystore).
- Backup em nuvem desativado (`allowBackup=false` + `data_extraction_rules`).
- `RawNotificationDao.purgeParsedContent()` apaga o conteúdo bruto mantendo o hash para dedup.

## Limitações conhecidas

- `NotificationListenerService` pode ser desvinculado pelo sistema; `NotificationAccess.rebind()` existe, mas convém orientar o usuário a isentar o app da otimização de bateria.
- Notificação agrupada/resumo é descartada (`FLAG_GROUP_SUMMARY`).
- Valores em notificações sem `R$` explícito não são extraídos.

## Licença

MIT — ver `LICENSE`.

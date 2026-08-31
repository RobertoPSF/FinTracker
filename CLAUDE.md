# FinTracker — contexto do projeto

App Android que lê notificações de bancos, classifica transações (entrada/saída) e monta extrato e balanço mensal. Local-first: nenhum dado sai do aparelho. Banco criptografado com SQLCipher. Open source (MIT).

Raiz: `C:\Users\rfilh\StudioProjects\FinTracker`
Package: `com.rfilh.fintracker`

---

## 1. Requisitos

### Funcionais

| ID | Requisito | Status |
|---|---|---|
| RF01 | Capturar notificações dos apps de banco via `NotificationListenerService` | feito |
| RF02 | Persistir notificação bruta antes de qualquer parsing | feito |
| RF03 | Deduplicar notificações reentregues | feito |
| RF04 | Classificar tipo (entrada/saída) por keywords | feito |
| RF05 | Extrair valor por regex | feito |
| RF06 | Extrair contraparte (best-effort) | feito |
| RF07 | Atribuir nível de confiança ao parse | feito |
| RF08 | Fila de revisão manual para baixa confiança / `UNKNOWN` | feito |
| RF09 | Extrato por banco, navegável por mês | feito |
| RF10 | Balanço consolidado por mês | feito |
| RF11 | Edição manual de transação | feito |
| RF12 | Lançamento manual de transação | feito |
| RF13 | Marcar transação como empréstimo, com pessoa e status | feito |
| RF14 | Tela "A cobrar" consolidada por pessoa | feito |
| RF15 | Importar extrato OFX/CSV com dedup contra o já capturado | feito |
| RF16 | Exportar transações em CSV | feito (sem UI) |
| RF17 | Diagnóstico: capturar qualquer app e exibir texto bruto | feito |
| RF18 | Regras de parsing externas ao código (JSON), com override em runtime | feito |
| RF19 | Bloqueio biométrico/PIN na abertura | **pendente** |
| RF20 | Importar/restaurar backup (JSON) | **pendente** |
| RF21 | Descartar conteúdo bruto pós-parse pela UI | backend pronto, **sem UI** |
| RF22 | Categorização automática por keyword de estabelecimento | **pendente** |
| RF23 | Gráfico mensal entrada vs saída | **pendente** |
| RF24 | Detecção de recorrência | **pendente** |
| RF25 | Alerta de limite por categoria | **pendente** |
| RF26 | Exportação anonimizada de notificação não reconhecida | **pendente** |

### Não funcionais

- Sem rede: o app não faz nenhuma requisição HTTP. Não adicionar permissão `INTERNET`.
- Banco criptografado (SQLCipher), passphrase de 32 bytes em `EncryptedSharedPreferences` (Keystore).
- Backup em nuvem desativado (`allowBackup=false` + `data_extraction_rules`).
- Camada de parsing sem dependência de Android, testável em `testDebugUnitTest`.
- Migração Room versionada desde a v1; toda mudança de schema exige nova versão + `Migration`.
- Nenhum dado pessoal real em testes ou commits.

---

## 2. Stack

- Kotlin 2.0.20, JDK 17
- AGP 8.5.2, Gradle 8.9, version catalog em `gradle/libs.versions.toml`
- compileSdk/targetSdk 34, minSdk 24
- Jetpack Compose (BOM 2024.09.02), Material 3, Navigation Compose 2.8.1
- Room 2.6.1 + KSP
- SQLCipher `net.zetetic:sqlcipher-android:4.6.1` + `androidx.sqlite:sqlite:2.4.0`
- `androidx.security:security-crypto:1.1.0-alpha06`
- `androidx.biometric:biometric-ktx:1.2.0-alpha05` (dependência incluída, ainda não usada)
- kotlinx-serialization-json 1.7.1
- DI manual (`AppContainer`), sem Hilt/Koin
- Módulo Gradle único (`:app`); separação por pacotes

---

## 3. Estrutura

```
app/src/main/java/com/rfilh/fintracker/
  AppContainer.kt          DI manual (db, repository, ingestor, rules, classifier)
  FinTrackerApp.kt         Application, expõe container
  MainActivity.kt          host do Compose

  core/
    Money.kt               formatação BRL, parseBrlToCents ("1.234,56" -> 123456)
    Dates.kt               yearMonth, shift, label, dayLabel, formatDate, parseDate
    Hashing.kt             sha256, notificationHash (janela de 60s)
    TextNormalizer.kt      minúsculas, sem acento, espaços colapsados

  parser/                  SEM dependência de Android
    ParserRules.kt         modelo das regras; bank() casa package, aliases e matchPrefix
    ParseResult.kt         Ignored | Parsed(bankName, type, amountCents, counterparty, confidence, matchedKeyword)
    TransactionClassifier.kt
    RulesProvider.kt       filesDir/parser_rules_override.json -> fallback asset

  notification/
    BankNotificationListener.kt   NotificationListenerService
    NotificationExtractor.kt      lê TITLE_BIG/TITLE, BIG_TEXT/TEXT/TEXT_LINES/SUMMARY/INFO/SUB/ticker
    NotificationIngestor.kt       raw -> dedup -> persistência -> classificação -> transação
    NotificationAccess.kt         checagem de permissão, deep link, requestRebind
    CaptureSettings.kt            flag "capturar todos os apps" (modo diagnóstico)

  domain/
    model/Models.kt        TransactionType, Confidence, LoanStatus, Transaction,
                           MonthBalance, BankSummary, PersonBalance, RawCapture, ImportSummary
    repo/FinanceRepository.kt   interface

  data/
    db/Entities.kt         RawNotificationEntity, TransactionEntity + rows de projeção
    db/Daos.kt             RawNotificationDao, TransactionDao
    db/Converters.kt       enums <-> String
    db/AppDatabase.kt      version = 1, MIGRATIONS vazio
    db/Passphrase.kt       32 bytes aleatórios em EncryptedSharedPreferences
    db/DatabaseFactory.kt  Room + SupportOpenHelperFactory (SQLCipher)
    importer/StatementImporter.kt   OFX e CSV, Kotlin puro
    repo/FinanceRepositoryImpl.kt
    repo/Mappers.kt
    repo/Exporter.kt       CSV

  ui/
    nav/FinTrackerNavHost.kt   rotas e factories de ViewModel
    vm/ViewModels.kt           Home, Statement, Review, Edit, Receivables, Diagnostics, Import
    screens/Components.kt      MonthBar, TransactionRow, cores
    screens/HomeScreen.kt
    screens/StatementScreen.kt
    screens/ReviewScreen.kt
    screens/EditScreen.kt
    screens/ReceivablesScreen.kt
    screens/DiagnosticsScreen.kt
    screens/ImportScreen.kt

app/src/main/assets/parser_rules.json
app/src/test/java/com/rfilh/fintracker/
  core/CoreTest.kt
  parser/TransactionClassifierTest.kt
  data/importer/StatementImporterTest.kt
```

Total: ~2.560 linhas de Kotlin.

---

## 4. Modelo de dados (Room, version = 1)

`raw_notification`
```
id, packageName, title, body, postedAt, hash (UNIQUE), parsed, purged
índices: hash (único), postedAt
```
`transactions`
```
id, bankPackage, bankName, type, amountCents (Long, centavos), counterparty,
occurredAt (epochMillis), yearMonth ("yyyy-MM"), confidence, reviewed,
category, note, rawId, loanStatus, loanPerson, loanAmountCents
índices: yearMonth, bankPackage, occurredAt, reviewed, loanPerson
```

Enums persistidos como String: `TransactionType` (INCOME/EXPENSE/UNKNOWN), `Confidence` (HIGH/MEDIUM/LOW), `LoanStatus` (NONE/LENT_PENDING/LENT_PAID/BORROWED_PENDING/BORROWED_PAID).

Valores monetários sempre em **centavos (Long)**. Nunca `Double`/`Float` em persistência.

---

## 5. Pipeline de captura

```
onNotificationPosted
  -> pacote é banco conhecido? (rules.bank(pkg) != null) ou modo diagnóstico ligado?
  -> NotificationExtractor.extract  (título + corpo, vários estilos de notificação)
  -> descarta FLAG_GROUP_SUMMARY sem "R$"
  -> NotificationIngestor.ingest(incoming, classify = isBank)
       insert raw com hash SHA-256("pkg|title|body|postedAt/60000")
         OnConflictStrategy.IGNORE -> retorno -1 = duplicata, encerra
       classify = false (diagnóstico) -> para aqui
       TransactionClassifier.classify
         Ignored -> marca raw como parsed, encerra
         Parsed  -> insere TransactionEntity, marca raw como parsed
```

### Regra de decisão do classificador

1. Normaliza `título + corpo`.
2. Keywords de `income` e `expense` (banco + genéricas): vence a que aparece **antes** no texto.
3. Se tipo `UNKNOWN` e bate lista `ignore` → `Ignored`.
4. Extrai valor por regex (`R$ 1.234,56` → 123456).
5. Se tipo `UNKNOWN` e sem valor → `Ignored`.
6. Extrai contraparte por regex, limpa e restaura acentos/caixa a partir do texto original.
7. Confiança: `HIGH` (banco conhecido + tipo + valor), `MEDIUM` (banco desconhecido), `LOW` (sem valor).

### Formato de `parser_rules.json`

```json
{
  "version": 1,
  "generic": {
    "ignore": [], "income": [], "expense": [],
    "amountPatterns": [], "counterpartyPatterns": []
  },
  "banks": [
    {
      "package": "br.com.bb.android",
      "name": "Banco do Brasil",
      "aliases": ["br.com.bb.android.tablet"],
      "matchPrefix": true,
      "ignore": [], "income": [], "expense": [],
      "amountPatterns": [], "counterpartyPatterns": []
    }
  ]
}
```
Regras do banco são avaliadas antes das genéricas. Grupo 1 das regex é o valor capturado.

---

## 6. Importação de extrato

Existe porque Nubank e Mercado Pago **não emitem notificação de Pix enviado**.

`StatementImporter.parse(content)` detecta OFX (`<STMTTRN`) ou CSV.
- OFX: `DTPOSTED`, `TRNAMT`, `MEMO`/`NAME`.
- CSV: delimitador `,` ou `;` autodetectado; cabeçalho procurado por `data|date|dt`, `valor|amount|value`, `descricao|historico|title|...`; datas `dd/MM/yyyy` ou `yyyy-MM-dd`; valores `-1.234,56` ou `-1234.56`.
- Sinal negativo → `EXPENSE`; positivo → `INCOME`.

Dedup em duas camadas em `FinanceRepositoryImpl.importStatement`:
1. `raw_notification.hash` = sha256(`import|pkg|occurredAt|amountCents|desc`), índice único.
2. `transactionDao.countSimilar(pkg, amountCents, ±12h)` contra o já capturado por notificação.

Transações importadas entram com `confidence = HIGH` e `reviewed = true`.

---

## 7. Navegação

| Rota | Tela |
|---|---|
| `home` | balanço do mês, lista por banco, atalhos |
| `statement?bank={pkg}` | extrato (bank vazio = geral) |
| `review` | fila de revisão |
| `edit/{id}` | edição; `id = 0` cria lançamento manual |
| `receivables` | a cobrar por pessoa |
| `diagnostics` | captura bruta e switch "todos os apps" |
| `import` | importação OFX/CSV |

ViewModels criados por `viewModelFactory { initializer { ... } }` com o `AppContainer`.

---

## 8. Convenções

- Comentário só onde a decisão não é óbvia pelo código.
- Nada de `localStorage`-like em UI: estado em `StateFlow` no ViewModel; Compose só lê.
- Queries expõem `Flow`; escrita em `suspend`.
- Camada `parser` e `data/importer` continuam **sem import de `android.*`** (quebra os testes).
- Todo banco novo entra em `parser_rules.json` + teste com string real anonimizada.
- Toda mudança de schema: `version++` em `AppDatabase` + `Migration` em `MIGRATIONS`.
- Strings de UI em português.

---

## 9. Build e testes

```
.\gradlew.bat :app:testDebugUnitTest     # parser, importer, core
.\gradlew.bat installDebug
```
Gradle JDK 17 (JBR 25 não funciona com AGP 8.5.2). Wrapper gerado pelo Android Studio no primeiro sync.

Cobertura atual dos testes: classificação (Nubank, Inter, C6, PicPay, BB, prefixo de pacote, banco desconhecido, ignore de fatura/saldo/marketing), parse de valor BRL, datas, hash de dedup, normalização, CSV (`,` e `;`), OFX.

---

## 10. Limitações conhecidas

- `NotificationListenerService` pode ser desvinculado pelo sistema; existe `NotificationAccess.rebind()`, mas o usuário precisa isentar o app da otimização de bateria.
- Notificação sem `R$` explícito não tem valor extraído.
- Importação lê só texto (OFX/CSV); PDF de comprovante não é suportado.
- `security-crypto` e `biometric` estão em alpha.
- Sem ícone próprio (usa `@android:drawable/sym_def_app_icon`).
- Sem tema escuro definido; usa `Theme.Material.Light.NoActionBar`.

---

## 11. Backlog priorizado

1. **RF19** bloqueio biométrico: `BiometricPrompt` na `MainActivity`, flag em `EncryptedSharedPreferences`, tela de configurações.
2. **RF21** UI para `purgeRawContent()` + toggle "descartar conteúdo bruto após parse".
3. **RF16/RF20** tela de backup: exportar CSV/JSON via `ACTION_CREATE_DOCUMENT`, importar JSON com merge por hash.
4. **RF22** categorização por keyword de estabelecimento (novo bloco `categories` em `parser_rules.json`).
5. **RF23** gráfico entrada vs saída por mês.
6. **RF24** detecção de recorrência (mesmo valor + contraparte em meses consecutivos).
7. **RF25** limite por categoria com notificação local.
8. **RF26** exportar notificação não reconhecida anonimizada (mascarar nomes e valores) para contribuição.
9. Ícone, tema escuro, string resources.
10. Release: assinatura, F-Droid metadata, GitHub Actions rodando os testes.
# FinTracker

App Android que lê as notificações dos seus bancos, classifica cada transação automaticamente (entrada/saída, categoria) e monta extrato e balanço mensal — sem depender de você lançar nada manualmente. **100% local**: o app não tem permissão de internet, nada sai do aparelho, e o banco de dados é criptografado (SQLCipher).

## Como baixar

O APK é publicado automaticamente a cada versão em **[Releases](https://github.com/RobertoPSF/FinTracker/releases/latest)** — esse link sempre aponta pra versão mais recente.

1. Baixe o `.apk` da release mais recente pelo celular.
2. Como o app não vem da Play Store, o Android vai pedir permissão pra instalar de "fonte desconhecida" na primeira vez — normal, é só confirmar.
3. Abra o app e conceda o acesso a notificações quando ele pedir (é o que permite capturar os lançamentos dos bancos automaticamente).

Requer Android 7.0 (API 24) ou superior.

## Possiveis problemas

Caso o celular bloqueie a instalação do AKP:

1. Abra a Play Store → toque no seu ícone de perfil (canto superior direito) → Play Protect → ícone de engrenagem (⚙️) → desative "Verificar apps com o Play Protect".
2. Volte e instale o APK de novo.
3. Pode reativar o Play Protect depois de instalado.

Caso não consiga habilitar a leitura de notificações:

1. Ajustes → Apps → FinTracker (ou segurar o ícone do app → "Informações do app")
2. Tocar no menu de três pontinhos (⋮) no canto superior direito da tela
3. Selecionar "Ativar configurações restritas" (ou "Allow restricted settings", dependendo do idioma/versão)

## O que o app faz hoje

**Captura e classificação automática**
- Lê as notificações dos apps de banco/carteira instalados (lista em `parser_rules.json`, 16 bancos já cobertos: Nubank, Inter, Itaú, Itaú iti, C6, PicPay, Mercado Pago, Caixa, Banco do Brasil, Santander, Bradesco, Neon, PagBank, Will Bank, Original, Google Pay).
- Classifica cada notificação como entrada, saída ou desconhecida, extrai o valor e (quando dá) a contraparte.
- Categoriza automaticamente por palavra-chave do estabelecimento (Alimentação, Transporte, Assinaturas, Compras, Saúde, Moradia).
- Deduplica notificações reentregues pelo sistema.
- O que fica com baixa confiança cai numa fila de revisão manual, em vez de virar transação errada silenciosamente.

**Visão financeira**
- Extrato geral ou por banco, navegável mês a mês.
- Balanço do mês (entradas, saídas, saldo) na tela inicial.
- Gráfico de entradas x saídas — visão mensal (últimos 6 meses) ou anual (Jan-Dez do ano corrente).
- Detecção automática de gastos recorrentes (mesma contraparte e valor parecido em meses seguidos).

**Empréstimos**
- Marcar uma transação como "emprestei" ou "peguei emprestado", com pessoa e status.
- Tela "A cobrar" consolidando quanto cada pessoa te deve.

**Edição e lançamento manual**
- Qualquer transação capturada pode ser editada (tipo, valor, contraparte, categoria, observação).
- Lançamento manual pra quando não veio notificação nenhuma.

**Privacidade e segurança**
- Bloqueio biométrico ou por PIN na abertura do app.
- Descarte automático (ou sob demanda) do texto bruto das notificações depois que já viraram transação.
- Exportação anonimizada de notificações que o app não conseguiu classificar — útil pra reportar um banco novo sem expor dados pessoais.
- Backup e restauração das transações em JSON.
- Limites de gasto por categoria, com aviso quando estourar no mês.

**Aparência**
- Tema claro e escuro (alternável nas configurações).

## Segurança técnica

- Sem permissão `INTERNET` — o app não faz nenhuma requisição de rede.
- Banco de dados Room + SQLCipher, com passphrase de 32 bytes aleatórios guardada em `EncryptedSharedPreferences` (Android Keystore).
- Backup em nuvem do próprio Android desativado (`allowBackup=false`).

## Para desenvolvedores

### Build local

1. Abrir a pasta no Android Studio e sincronizar (o wrapper do Gradle já está commitado no repo).
2. `./gradlew :app:testDebugUnitTest` — testes de parser/classificador/core (Kotlin puro, sem Android).
3. `./gradlew :app:installDebug` — instala no emulador/aparelho conectado.

Requisitos: JDK 17, AGP 8.13.2, Kotlin 2.0.20, compileSdk 34, minSdk 24.

### Como contribuir

Basta abrir um PR novo para o repositório.

# Controle de Pagamentos

Aplicativo Android local-first para controlar pagamentos manuais, recorrentes, parcelados e capturas revisaveis de notificacoes financeiras.

## Stack

- Kotlin
- Jetpack Compose
- Room
- DataStore
- NotificationListenerService

## Rodando

Abra a pasta no Android Studio ou use o Gradle Wrapper:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

O APK debug fica em:

```txt
app/build/outputs/apk/debug/app-debug.apk
```

Para testar captura real, instale em um aparelho Android, abra `Ajustes > Permissao do Android` dentro do app e habilite o listener. Depois ative explicitamente os apps financeiros em `Ajustes > Apps monitorados`.

## Testando tetos

Na aba `Relatorios`, toque em `Novo` dentro de `Tetos de gastos`.

- Sem categoria selecionada: cria um teto geral do mes.
- Com categoria selecionada: cria um teto especifico para aquela categoria.
- O progresso aparece em `Relatorios` e o teto geral tambem aparece no `Dashboard`.

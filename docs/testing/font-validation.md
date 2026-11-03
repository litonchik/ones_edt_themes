# Проверка 0.1.1 — шрифт кода

Дата: 14 октября 2026. Проверяется копия EDT 2026.1.3+25 Windows x64
в `.work/edt-test`, тестовая рабочая область `.work/workspace-probe`.
Рабочая EDT и рабочие области пользователя не изменялись. Пользователь подтвердил
работу предыдущего MVP 0.1.0, затем одобрил краткий дизайн шрифта.

Целевая среда совпадает с [проверкой MVP](mvp-validation.md): Eclipse 4.30.0,
Xtext UI 2.33.0, EDT BSL UI 23.0.1, theming UI 2.1.0, Axiom Java 17.0.18,
Maven 3.9.11 по полному пути, Tycho 4.0.13. Глобальные переменные среды не менялись.

## Сборка и RED → GREEN

Новые unit tests сначала остановились на отсутствующем `SnapshotService.applyExplicit`.
После реализации **35 tests, failures=0, errors=0, skipped=0**.
Обычный apply сохраняет прежнюю защиту ручных цветов; explicit apply позволяет пользователю
выбрать шрифт после ручной правки штатной настройки и сохраняет первоначальный снимок.

Runtime test был запущен на установленном старом 0.1.0 и получил ожидаемый
`FAIL font size selector exists`. Финальная 0.1.1 прошла:

| Запуск | Exit | PASS | FAIL |
|---|---:|---:|---:|
| FontApply | 0 | 39 | 0 |
| FontRestore, новый JVM процесс | 0 | 9 | 0 |
| Apply, регрессия цветов | 0 | 878 | 0 |
| Restore, новый JVM процесс | 0 | 100 | 0 |

Проверено: предпросмотр не пишет native preferences; Cancel до Apply сохраняет шрифт;
Apply меняет уже открытый настоящий BSL StyledText; выбор наследуется новым BSL editor;
переключение Darcula/Monokai и восстановление цветов не сбрасывают шрифт;
повторный явный выбор заменяет последующую native font edit.
Кнопка возврата сначала меняет только pending выбор, Cancel его отменяет.
Apply возвращает точное исходное значение или отсутствие override.
Отдельно проверен заранее явно заданный native FontData[] и его эффективный шрифт.
Настоящая кнопка OK подтверждает выбор; после перезапуска JVM проверен BSL font,
затем возвращены исходное значение и Courier New 10, font journal очищен.
Тестовый выбор: Consolas 15. Это fixture, а не навязанный пользователю default.

Диалог страницы открыт и визуально просмотрен: выбор семейства/размера, весь образец,
кнопка возврата и Apply помещаются в страницу. Ресурсы preview Font освобождаются при
смене и закрытии страницы; в финальных assertions нет failures.

[Снимок страницы](screenshots/font-preferences-preview.png),
[BSL с выбранным шрифтом](screenshots/font-chosen-workbench.png).
Точные assertions: [FontApply](0.1.1/runtime-fontapply.txt),
[FontRestore](0.1.1/runtime-fontrestore.txt),
[цвета Apply](0.1.1/runtime-apply.txt), [цвета Restore](0.1.1/runtime-restore.txt).

Финальный Verify: **BUILD SUCCESS**, bundle **0.1.1.202610140722**,
23 class-files major **61**, ZIP содержит только собственные bundle/feature и p2 metadata.
Архив `dist/edt-themes-0.1.1.zip`: **64 786 bytes**.
SHA-256: `cfd3a2954778179a1318040e0f6aeeda89d63391fb32d923e35ac7f01c129785`.
Сумма проверена после runtime прогонов; ZIP 0.1.0 сохранён отдельно.

## Воспроизведение

```powershell
& .\scripts\build.ps1 -Action Verify
& .\scripts\p2-test.ps1 -Action Install -Zip .\dist\edt-themes-0.1.1.zip
& .\scripts\build-workbench-probe.ps1
& .\scripts\run-workbench-probe.ps1 -Phase FontApply
& .\scripts\run-workbench-probe.ps1 -Phase FontRestore
& .\scripts\run-workbench-probe.ps1 -Phase Apply
& .\scripts\run-workbench-probe.ps1 -Phase Restore
& .\scripts\run-workbench-probe.ps1 -Phase Reconcile
& .\scripts\p2-test.ps1 -Action Uninstall
& .\scripts\p2-test.ps1 -Action Audit
```

Тестовая копия должна быть предварительно подготовлена `prepare-test-edt.ps1`.
Reconcile удаляет test-only fragment, который EDT автоматически регистрирует как locked
dropins IU. Потребительский ZIP такого fragment не содержит.
Тестовый p2 director использует `-Xms80m -Xmx4096m`, как штатная EDT: 1024 MiB снова
оказалось недостаточно при старте OSGi. Неудачный запуск до установки не засчитывался.
INI установленной EDT не изменён.

Установка обновления 0.1.0 → 0.1.1, удаление после восстановления и финальный Audit
завершились **exit 0**. В копии нет активных snapshot записей цветов/шрифта и feature/bundle.
Сравнение с исходной установленной EDT подтвердило те же **1267 базовых ID/версий bundles**.
Синтаксис всех шести PowerShell scripts проверен штатным parser.

Сборка: `.work/font-verify-final.log`; служебные runtime логи: `.work/runtime-*.log`;
p2: `.work/font-install-final.log`, `.work/font-uninstall-final.log`, `.work/font-audit-final.log`.
Ограничения vendor startup/shutdown logs и ручная матрица preview/MXL/graph/DCS,
полного metadata project и повышенного DPI остаются из MVP.
Не заявляется проверка всех установленных шрифтов или редакторов с индивидуальными font overrides.

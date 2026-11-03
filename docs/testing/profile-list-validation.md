# Читаемость списка палитр — 0.1.3

Дата: 14 октября 2026. Пользователь воспроизвёл белую выбранную строку с невидимым
названием Darcula в списке палитр. Этот список оставался нативным Combo после
исправления списка шрифтов в 0.1.2.

## Изменение

Обобщён существующий FontFamilyPicker в ReadableCombo. Список палитр и список
шрифтов используют один popup Table с явной отрисовкой фона и текста выбранной
строки через публичные SWT EraseItem/PaintItem. Цвета берутся из selection roles
выбранной палитры, для штатного оформления — из парных системных цветов SWT.
Программный select(int) не отправляет Selection. Popup освобождается до локальных
Color при закрытии страницы. Сохранение и восстановление preferences не менялись.

## Проверка

Все действия — в `.work/edt-test` и `.work/workspace-probe`, без Git и субагентов.
Рабочая EDT не изменялась. Target: EDT **2026.1.3+25 Windows x64**, Eclipse **4.30.0**,
SWT **3.124.200**, Xtext UI **2.33.0**, Axiom JDK **17.0.18**, Maven **3.9.11**,
Tycho **4.0.13**.

Verify: **BUILD SUCCESS**, **38 unit tests**, failures=0, errors=0, skipped=0.
Bundle **0.1.3.202610140903**, **26 class-files major 61**. ZIP содержит только
собственные bundle/feature и обе палитры, без test fragment.

Preview: **133 PASS, 0 FAIL**. Проверены RGB preview, три строки popup при Darcula
и Monokai, выбор мышью, Enter, отмена строки через Escape. Pending выбор меняет
образец, но не применяет профиль и не записывает шрифт до Apply/OK.

Регрессия цветов: **878 Apply PASS**, **100 Restore PASS**, **0 FAIL**.
Проверены реальный BSL editor, переключения профилей, ручные изменения,
штатная Light, настоящий Apply/Cancel и возврат исходных значений в новом JVM.

Регрессия шрифта: **39 FontApply PASS**, **9 FontRestore PASS**, **0 FAIL**.
Проверены реальные BSL редакторы, Apply/OK/Cancel, независимость от цвета,
сохранение после перезапуска и точное восстановление исходного override/его отсутствия.
Код проверен автором; независимый reviewer не запускался по указанию пользователя.

По пикселям выбранной строки измерен контраст **8.64:1 Monokai**, **9.67:1 Darcula**.
Область измерения ограничена собственным видимым popup; снимки рабочего стола не
сохранялись. Снимки обеих палитр просмотрены: выбранная Darcula читается.

Архив: `dist/edt-themes-0.1.3.zip`, **73 607 bytes**.
SHA-256: `7123819ab9517511646cc0ea9786af188df13e9ba0dac5b5db58494da8486fbe`.
Предыдущие ZIP сохранены. Ограничения визуальной приёмки всех экранов и повышенного
DPI из MVP остаются.

P2 Install/Uninstall/Audit завершились **exit 0**; test-only fragment удалён через
Reconcile. Активные журналы цветов и шрифта пусты, feature/bundle отсутствуют в
тестовой копии. **1267 базовых ID/версий bundles** совпадают с рабочей EDT.
SHA-256 архива повторно подтверждена после runtime-проверок.
Первый sandbox-запуск p2 получил exit 13 (копия определена как read-only);
повтор в разрешённом контексте успешно установил тот же ZIP только в `.work/edt-test`.

## Воспроизведение

```powershell
& .\scripts\build.ps1 -Action Verify
& .\scripts\p2-test.ps1 -Action Install
& .\scripts\build-workbench-probe.ps1
& .\scripts\run-workbench-probe.ps1 -Phase Preview
& .\scripts\run-workbench-probe.ps1 -Phase Apply
& .\scripts\run-workbench-probe.ps1 -Phase Restore
& .\scripts\run-workbench-probe.ps1 -Phase FontApply
& .\scripts\run-workbench-probe.ps1 -Phase FontRestore
& .\scripts\run-workbench-probe.ps1 -Phase Reconcile
& .\scripts\p2-test.ps1 -Action Uninstall
& .\scripts\p2-test.ps1 -Action Audit
```

Логи сборки/установки: `.work/profile-verify.log`, `.work/profile-install.log`;
runtime: `.work/runtime-*.txt`, `.work/runtime-*.log`.

Снимки собственного popup: [Monokai](0.1.3/monokai-profile-list.png),
[Darcula](0.1.3/darcula-profile-list.png). [Preview assertions](0.1.3/runtime-preview.txt).
Регрессия: [Apply](0.1.3/runtime-apply.txt), [Restore](0.1.3/runtime-restore.txt),
[FontApply](0.1.3/runtime-fontapply.txt), [FontRestore](0.1.3/runtime-fontrestore.txt).

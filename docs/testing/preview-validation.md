# Исправления UI — 0.1.2

Дата: 14 октября 2026. Пользователь подтвердил выбор шрифта и прислал два дефекта:
неокрашенный образец кода и нечитаемое имя шрифта на белой выбранной строке в Dark/Monokai.

## Причины и изменения

Прежний StyledText не содержал StyleRange. Добавлен фиксированный RU/EN образец с
комментарием, директивами, строками и числовым литералом; цвета и bold берутся из ролей
выбранной палитры. Предпросмотр переключается сразу без записи preferences.
Checkbox BSL отключает раскраску образца; «Штатное оформление» показывает обычный текст.

Нативный Combo не предоставляет API цветов выбранной строки. Попытка заменить его
на CCombo с List не прошла pixel-проверку. В итоговой реализации FontFamilyPicker
показывает собственный popup Table и через публичные EraseItem/PaintItem рисует фон
и текст выбранной строки одновременно. Нет reflection, Win32 API вызовов или vendor patches.
Enter подтверждает выделенный шрифт, Escape закрывает popup без смены имени.
Popup и локальные цвета освобождаются при закрытии страницы.

## Проверенные результаты

Целевая платформа прежняя: EDT **2026.1.3+25 Windows x64**, Eclipse 4.30.0,
SWT 3.124.200, Xtext UI 2.33.0, Axiom JDK **17.0.18**, Maven **3.9.11**, Tycho **4.0.13**.
Все действия выполнялись в `.work/edt-test` и `.work/workspace-probe`.

RED: новые unit tests не компилировались без CodePreview; старый ZIP 0.1.1 получил
`FAIL preview renders Monokai keyword color`. Первая pixel-проверка CCombo не прошла.
Для оценки текущего кадра требуется repaint/короткое ожидание композиции окна;
результаты мгновенного screen copy после открытия popup не засчитываются как окончательные.

GREEN: **38 unit tests, failures=0, errors=0, skipped=0**. Финальный Verify — **BUILD SUCCESS**.
Bundle **0.1.2.202610140828**, **26 class-files major 61**.

Runtime на финальной сборке: **109 Preview PASS, 0 FAIL**; регрессия FontApply —
**39 PASS**, FontRestore в новом JVM — **9 PASS**, без failures.
Проверены фактические RGB StyledText и токенов обеих палитр, pending выбор другой палитры,
BSL checkbox, отсутствие записи native font preferences при выборе в popup,
мышь, Enter/Escape, применение и восстановление размера/семейства, native explicit/absent
original и перезапуск. При смене семейства предпросмотр сохраняет свои StyleRange.

Измеренный контраст glyph/background выбранной строки:

| Палитра | Фон выбранной строки | Контраст |
|---|---|---:|
| Monokai | RGB 73,72,62 | 8.64:1 |
| Darcula | RGB 33,66,131 | 9.67:1 |

Контраст измеряется по пикселям только собственного видимого popup, после обработки
repaint; native фокусная рамка/scrollbar исключены из области измерения.
Полный рабочий стол не сохраняется в отчёт. Preview RGB дополнительно проверяются
публичными getStyleRangeAtOffset/getBackground.

Архив: `dist/edt-themes-0.1.2.zip`, **73 522 bytes**.
SHA-256: `1b04ece6c45577fb69943caac4ed346e10a4923dc09502f9ffd320f4bfeb5dba`.
Предыдущие ZIP сохранены отдельно; журналы 0.1.0/0.1.1 совместимы.
Ограничения проверки всех экранов и повышенного DPI из MVP остаются.

## Воспроизведение

```powershell
& .\scripts\build.ps1 -Action Verify
& .\scripts\p2-test.ps1 -Action Install -Zip .\dist\edt-themes-0.1.2.zip
& .\scripts\build-workbench-probe.ps1
& .\scripts\run-workbench-probe.ps1 -Phase Preview
& .\scripts\run-workbench-probe.ps1 -Phase FontApply
& .\scripts\run-workbench-probe.ps1 -Phase FontRestore
& .\scripts\run-workbench-probe.ps1 -Phase Reconcile
& .\scripts\p2-test.ps1 -Action Uninstall
& .\scripts\p2-test.ps1 -Action Audit
```

Логи: `.work/preview-verify-release.log`, `.work/preview-install-release.log`,
`.work/runtime-preview.log`, `.work/runtime-font*.log`, `.work/preview-uninstall.log`,
`.work/preview-audit.log`. Assertions — `docs/testing/0.1.2`.
Vendor startup/shutdown diagnostic messages не приравниваются к errors собственных проверок.

Финальные p2 Install/Uninstall/Audit завершились **exit 0**. Активные журналы цвета и шрифта
пусты, собственные feature/bundle удалены из копии. Все **1267 базовых ID/версий bundles**
совпадают с исходной установленной EDT. Контрольная сумма ZIP подтверждена после проверки.

Снимки только собственного popup:
[Monokai](0.1.2/monokai-font-list.png), [Darcula](0.1.2/darcula-font-list.png).
Assertions: [Preview](0.1.2/runtime-preview.txt),
[FontApply](0.1.2/runtime-fontapply.txt), [FontRestore](0.1.2/runtime-fontrestore.txt).

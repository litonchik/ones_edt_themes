# Проверка редактора палитры 0.1.4

Дата: 14 октября 2026, Europe/Moscow. Платформа: **1С:EDT 2026.1.3+25,
Windows x64**, Eclipse 4.30.0, SWT 3.124.200.v20231113-1355,
Xtext UI 2.33.0.v20231121-0955, EDT BSL UI 23.0.1.v202609041212,
theming UI 2.1.0.v202609041212. Axiom JDK 17.0.18, Maven 3.9.11, Tycho 4.0.13.

Рабочая EDT не изменяется. Используется существующая `.work/edt-test`,
workspace `.work/workspace-probe`, изолированные user.home/APPDATA/LOCALAPPDATA/
USERPROFILE/TEMP/TMP внутри проекта. Дополнительное ПО не устанавливалось,
Git и субагенты не использовались.

## Сборка и модель

Verify BUILD SUCCESS. **51 unit test, 0 failures/errors**.
Новые сценарии: изоляция/нормализация draft maps, валидация HEX/ролей/профилей,
explicit reset, target flags, выборочный перехват ручных правок, native side effects,
atomic personal save/restore, write/flush/final commit failures, recovery и codec
pending entries без original, запрет ложного подтверждения сохранения при pending.
Старые тесты original absent/explicit, font explicit и журнала также прошли.

RED зафиксирован на отсутствующих API, затем на реальных дефектах personal conflict
recovery, native overwrite, pending-save и silent truncation HEX в SWT поле.
Runtime на старой 0.1.3 остановился на отсутствующем редакторе.
Первый runtime 0.1.4 прошёл функциональные сценарии; безопасный снимок был остановлен
из-за неполной видимости таблицы. Захват теперь ограничен пересечением контрола
со всеми видимыми родительскими областями. Захват широких окон в старых пробах
отключён; новые снимки относятся только к контролам/popup плагина.

## Runtime

Layout: **26 PASS, 0 FAIL** — все 17 пояснений
помещаются при текущем DPI, фон обычной строки однородный по всей ячейке,
снимки ограничены видимой областью контролов. Автор просмотрел таблицу,
описание роли и иллюстрацию интерфейса. Palette: **252 PASS**, PaletteRestart:
**14 PASS**, без failures. Полная регрессия также прошла:

| Phase | PASS | FAIL |
|---|---:|---:|
| PaletteLayout | 26 | 0 |
| Palette | 252 | 0 |
| PaletteRestart | 14 | 0 |
| Preview | 133 | 0 |
| Apply | 878 | 0 |
| Restore | 100 | 0 |
| FontApply | 39 | 0 |
| FontRestore | 9 | 0 |
| Всего | **1451** | **0** |

Предыдущие результаты 0.1.3 не считаются проверкой нового UI.

Сценарий Palette: все 17 ролей, HEX, pending preview без записи EDT, черновики
обоих профилей, Cancel, ошибка HEX в другом профиле, Apply/OK/Cancel after Apply,
личный panel в native registry, ручные keyword/comment, выборочный reset,
сброс всей палитры, BSL=false и независимость шрифта, точный original restore.
PaletteRestart: обе личные карты в новом JVM, реальный BSL glyph, restore с
сохранением профилей, очистка тестовых overrides, oversized HEX rejection.
Регрессия: Preview, Apply/Restore, FontApply/FontRestore.

Layout RED: новый pixel-тест воспроизвёл белые полосы только под текстом.
Причина — заполнение event.width вместо всей ячейки и system list background
поверх Dark. Исправлена ширина заполнения и использованы штатные цвета таблицы
для обычных строк; selected text/background рисуются вместе.
Снимки Darcula: [роли](0.1.4/darcula-palette-roles.png),
[описание](0.1.4/darcula-palette-usage.png),
[интерфейс](0.1.4/darcula-palette-interface-preview.png).
Снимки Monokai: [роли](0.1.4/monokai-palette-roles.png),
[интерфейс](0.1.4/monokai-palette-interface-preview.png).

## Артефакт

`dist/edt-themes-0.1.4.zip`, **89 223 bytes**.
Bundle/feature: **0.1.4.202610141024**, 29 classes, major 61, test-only код исключён.
SHA-256: `43ef21a82b6edd02f9b13131c56b6fba354e464bf71f11cd3754512d69be010d`.
Старые ZIP 0.1.0–0.1.3 сверены со своими SHA-256 и сохранены.
1267 базовых ID/версий bundles тестовой копии совпали с установленной EDT.
Сборка: [verify.log](0.1.4/verify.log). Assertions каждого этапа находятся
в `docs/testing/0.1.4/runtime-*.txt`.
P2 **Install / Uninstall / Audit: exit 0**. Reconcile выполнен; собственные
feature/bundle/test fragment удалены из конфигурации копии, activeProfile отсутствует,
текущие журналы цветов и шрифта пусты, тестовые personal overrides удалены.
Логи: [install](0.1.4/p2-install.log), [uninstall](0.1.4/p2-uninstall.log),
[audit](0.1.4/p2-audit.log). Базовые 1267 ID/версий повторно сверены после удаления.

## Воспроизведение

```powershell
$ErrorActionPreference = 'Stop'
& .\scripts\build.ps1 -Action Verify
& .\scripts\p2-test.ps1 -Action Install -Zip .\dist\edt-themes-0.1.4.zip
& .\scripts\build-workbench-probe.ps1
& .\scripts\run-workbench-probe.ps1 -Phase PaletteLayout
& .\scripts\run-workbench-probe.ps1 -Phase Palette
& .\scripts\run-workbench-probe.ps1 -Phase PaletteRestart
& .\scripts\run-workbench-probe.ps1 -Phase Preview
& .\scripts\run-workbench-probe.ps1 -Phase Apply
& .\scripts\run-workbench-probe.ps1 -Phase Restore
& .\scripts\run-workbench-probe.ps1 -Phase FontApply
& .\scripts\run-workbench-probe.ps1 -Phase FontRestore
& .\scripts\run-workbench-probe.ps1 -Phase Reconcile
& .\scripts\p2-test.ps1 -Action Uninstall
& .\scripts\p2-test.ps1 -Action Audit
```

## Ручная приёмка и границы

- Нативный ColorDialog (выбор/отмена мышью), DPI/масштаб Windows и все размеры
  окна остаются ручной матрицей. Runtime программно проверяет HEX путь и тот же
  updater preview/store. Используется штатный публичный JFace ColorSelector.
- Не проверялись все наполненные окна EDT, project preferences, формы, MXL,
  графическая схема и макет СКД: [матрица MVP](mvp-validation.md).
- UI образцы условные, профиль не является аудитом фактических цветов.
  Предупреждение о низком контрасте не добавлялось; пользователь может выбрать
  собственные сочетания. Списки ролей рисуют selected foreground/background вместе.
- В логах остаются сообщения штатных служб EDT (например, отсутствие wmic и
  duplicate extensions). Отсутствие всех штатных предупреждений не заявляется.
- Hover строк ReadableCombo отложен с самым низким приоритетом, как согласовано.

Автор выполняет самостоятельное ревью и визуальную проверку ограниченных снимков;
независимый reviewer не запускался по прямому указанию пользователя.

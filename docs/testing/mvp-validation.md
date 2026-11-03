# Проверка MVP 0.1.0

Дата: 13 октября 2026. Целевая EDT: **2026.1.3+25, Windows x64**.
Заключительный p2 audit выполнен 14 октября 2026: exit 0, feature/bundle удалены,
активного профиля нет, текущий журнал пуст; 1267 базовых bundles совпадают с исходной EDT.
Рабочая EDT пользователя не изменяется. Проверяемая копия:
`C:\Users\litonchik\Documents\Codex\ones_edt_theme_plugin\.work\edt-test`.
Рабочая область: `.work/workspace-probe`; APPDATA/LOCALAPPDATA/USERPROFILE/user.home
для runtime перенаправлены в каталоги `.work`.

## Проверяемые компоненты

| Компонент | Версия |
|---|---|
| Eclipse | 4.30.0 |
| SWT | 3.124.200.v20231113-1355 |
| Xtext UI | 2.33.0.v20231121-0955 |
| EDT theming UI | 2.1.0.v202609041212 |
| EDT BSL UI | 23.0.1.v202609041212 |
| JDK | Axiom 17.0.18 |
| Maven | 3.9.11, полный путь C:\apps\apache-maven-3.9.11\bin\mvn.cmd |
| Tycho | 4.0.13 |

## Автоматические проверки

`scripts/build.ps1 -Action Verify` выполняет unit tests, Tycho reactor и проверку архива.
33 unit tests охватывают RGB/роли, карту targets, отсутствие явного override, смену профилей,
сохранение ручных цветов, частичный отказ записи и flush, устойчивый журнал, Unicode,
защиту от повторного apply, BSL checkbox и наблюдение перед внешним native CSS overwrite.
Полный вывод последней сборки — `.work/final-verify.log`; отчёты JUnit — `tests/unit/target/surefire-reports`.

Проверка ZIP допускает один собственный bundle и одну feature, требует p2 metadata,
обе палитры и карты preferences. Все 21 class-file собственного bundle имеют major **61**.
Test-only код в release не включён. `dist/edt-themes-0.1.0.zip.sha256` содержит SHA-256.

Проверка p2 через `scripts/p2-test.ps1` устанавливает архив только в копию внутри проекта,
сравнивает ID/версии всех базовых bundles до и после. Для замены ранее установленной
нашей feature директор одновременно удаляет старую root IU и устанавливает новую.
Обычный `-installIU` добавлял вторую exact-version root и создавал конфликт — это исправлено
в тестовом скрипте, без изменения feature или базовой платформы.

## Runtime probe

`tests/workbench` — отдельный test-only fragment, который не поставляется пользователю.
Он запускает workbench на копии EDT и проверяет публичные API, реальные instance preferences,
ColorRegistry, SWT Shell, страницу настроек и перезапуск в новом JVM процессе.
Штатный startup EDT theming разрешён; остальные early-startup contributions ограничены
для воспроизводимого probe. Поэтому результат не является проверкой всех фоновых служб EDT.

Команды воспроизведения:

```powershell
& .\scripts\build.ps1 -Action Verify
& .\scripts\prepare-test-edt.ps1  # Только один раз: существующая копия не удаляется.
& .\scripts\p2-test.ps1 -Action Install -Zip .\dist\edt-themes-0.1.0.zip
& .\scripts\build-workbench-probe.ps1
& .\scripts\run-workbench-probe.ps1 -Phase Apply
& .\scripts\run-workbench-probe.ps1 -Phase Restore
& .\scripts\run-workbench-probe.ps1 -Phase Reconcile # Удаляет test-only fragment и его dropins регистрацию.
& .\scripts\p2-test.ps1 -Action Uninstall
& .\scripts\p2-test.ps1 -Action Audit
```

Выводы находятся в `.work/runtime-apply.txt`, `.work/runtime-restore.txt`;
служебные логи — в `.work/runtime-*.log`. Скрипт может показывать тестовое SWT окно.
Probe сохраняет Monokai в конце Apply для проверки следующего процесса; Restore возвращает исходные настройки.
После неудачного probe следующий Apply сначала восстанавливает предыдущий журнал.
В EDT reconciler может автоматически зарегистрировать вручную добавленный fragment как locked
dropins IU; без его удаления p2 сохраняет host bundle. Phase Reconcile перемещает только
test-only fragment внутри .work и запускает штатное reconciliation; release ZIP его не содержит.
Audit запускает новый OSGi/p2 процесс, проверяет отсутствие feature/bundle, активного профиля
и управляемых записей в текущем слоте журнала. Это проверка удаления и состояния preferences;
полный GUI старт EDT после uninstall отдельно не заявляется.

Итоговые Apply/Restore: **exit 0 / exit 0, 879 / 100 PASS, 0 FAIL**.
Сохранённые assertions: [Apply](runtime-apply.txt), [Restore](runtime-restore.txt).
Снимки: [Darcula](screenshots/darcula-workbench.png), [Monokai](screenshots/monokai-workbench.png),
[Monokai после перезапуска](screenshots/monokai-restarted-workbench.png).

В рамках разработки воспроизведены и исправлены: устаревший SWT color cache при same-ID reapply,
потеря исходного BSL при первом apply с выключенным checkbox, отсутствие общей ошибки restore
при отказе журнала с одними конфликтами, потеря наблюдённого ручного цвета после native CSS overwrite.
Первоначальный probe с `visible=false` не обеспечивал нормальную branding/theming и оставлял
светлый UI; он не засчитывается как визуальная проверка. Финальный probe использует видимый workbench,
проверяет RGB самого Shell и делает снимок после обработки repaint events.

Открыт настоящий BSL editor EDT на UTF-8 файле в обычном тестовом Eclipse project.
Проверяется RGB StyledText и glyph ranges комментария, русских/английских keywords,
числа, строки и директивы. Найденный stale lexical presentation исправлен через публичный
`ITextOperationTarget` → `ITextViewer.invalidateTextPresentation()`: исходный probe показывал
серый числовой литерал при правильном workspace preference; после исправления проверяется
реальная отрисовка без принудительного обновления из test-only кода.
Плагин не закрывает документы и не очищает undo history.

## Ручная матрица перед использованием с рабочими проектами

| Экран/сценарий | Что проверить | Статус |
|---|---|---|
| Навигатор, основные панели и вкладки | Обе палитры, читаемость текста и границ | Runtime Shell/registry проверяются; снимки отдельного workbench |
| Страница «Темы EDT» | Выбор, BSL, Apply, OK, Cancel, восстановление | Проверка зарегистрированной страницы в probe; подтверждение кнопки force restore вручную |
| BSL editor на UTF-8 fixture | Русские/английские keywords, comments, strings, numbers, directives | Реальный editor и RGB glyph ranges проверяются; полный metadata project требует ручной проверки |
| Запрос внутри BSL строки | Не обещать отдельную query palette; проверить штатное поведение | Вручную; fixture `tests/fixtures/theme-sample.bsl` |
| Error/warning/hints/console | Выделение, фокус, hover, disabled и сообщения | Targets проверены; наполненные экраны вручную |
| Второе окно/новый редактор | Цвета наследуются после выбора/перезапуска | Probe проверяет новый Shell и повторное открытие BSL; metadata project вручную |
| Обычный/повышенный масштаб Windows | Отступы, подписи, focus/selection, текст редактора | Вручную; текущий probe не изменяет scale ОС |
| Предпросмотр формы | Содержимое соответствует прикладному виду | Не проверено без тестовой конфигурации |
| MXL | Пользовательские цвета ячеек и печатного документа | Не проверено без тестового макета |
| Графическая схема | Прикладные цвета фигур/линий | Не проверено без тестового макета |
| Оформление СКД | Вид пользовательского макета | Не проверено без тестового макета |
| Подсветка строки при наведении в списках тем и шрифтов | Добавить hover в общем ReadableCombo: контрастный фон и текст под курсором, снятие подсветки при уходе; наведение не подтверждает выбор и не записывает настройки; сохранить клик, Enter/Escape и работу клавиатуры | Отложенная доработка по решению пользователя от 14.10.2026; последний, самый низкий приоритет. Ориентировочно 20–40 минут со сборкой и проверками |

Для BSL используйте fixture в модуле небольшой тестовой конфигурации. Для исключённых макетов
задайте собственные контрастные цвета и сравните до/после профиля; содержимое не должно меняться.
После подтверждённого Apply сделайте десять смен, повторно откройте редактор, перезапустите,
проверьте безопасный и принудительный возврат. Перед uninstall сначала восстановите оформление.

Копия EDT может писать диагностические сообщения штатных служб (standalone server/CLI,
duplicate contributions, shutdown jobs). Логи сохранены; полный штатный запуск и отсутствие
всех SWT/OSGi сообщений не заявляются на основании ограниченного probe.

Нативные Windows scrollbar остаются светлыми; отдельные категории прагм, меток и части
препроцессора сохраняют штатные цвета. Снимки показывают фактический вид ограниченного
workbench; эти особенности не выдаются за полное покрытие палитрой.

Финальный результат конкретных запусков, счётчики и hash фиксируются в `progress.md`.

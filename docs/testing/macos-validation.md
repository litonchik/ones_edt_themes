# Запуск на macOS — 0.1.5

Дата: 14 октября 2026. Пользователь разрешил минимальную поддержку Mac и потребовал
сохранить редактор палитры, реализованный в соседнем чате в 0.1.4.

## Изменение и границы поддержки

EdtCompatibility использует RuntimePlatform для проверки пары OS/architecture и Java.
Разрешены **win32/x86_64** и **macosx/x86_64**, Java **17**. Проверки Eclipse 4.30.0,
Xtext UI 2.33.0, BSL UI **23.0.1.v202609041212** и theming UI
**2.1.0.v202609041212** сохранены. Разрешение ОС не отключает контроль EDT версии.

Mac-кандидат предназначен для Intel Mac и x86 EDT/JVM через Rosetta на Apple Silicon.
Native aarch64 runtime, Linux и остальные архитектуры не разрешены. На Windows
собирается тот же Java bundle/feature, без новых vendor bundles и native libraries.
Mac target не добавлялся: он не нужен для сборки кандидата, а требовался бы для
отдельной проверки Mac dependency resolution на build host.

14 октября 2026 пользователь подтвердил работу этой версии на Mac сообщением
**«проверено, работает»**. Это подтверждение фактической работы пользователем;
самостоятельный запуск агентом на Mac не выполнялся. Модель Mac, версия macOS,
фактические EDT/JVM versions и перечень пройденных сценариев не сообщены.
Полная визуальная приёмка и идентичная отрисовка Cocoa/Win32 не заявляются.

## Сохранность 0.1.4

Перед правкой прочитан актуальный AGENTS.md и зафиксированы SHA-256 всех 29 существующих
файлов src/resources: `.work/mac-source-baseline.json`. Из существующих исходников
менялся только `edt/EdtCompatibility.java`; остальные исходники и ресурсы палитры
сверены с baseline. Добавлен независимый model RuntimePlatform и пять тестов.
Release metadata/scripts изменены на 0.1.5. ZIP 0.1.4 не перезаписывался.

Палитра, drafts/overrides, selective explicit apply, снимки/rollback и UI 0.1.4
используются в этой сборке. Hover строк остаётся отложенным.

## Проверено на Windows

RED: новая проверка Mac на прежней Windows-only политике получила ожидаемый отказ
`macosx/x86_64`; Mac Java-policy проверка также не проходила из-за запрета ОС.
Лог: `.work/mac-unit-red.log`.

GREEN: **56 unit tests, 0 failures, 0 errors, 0 skipped**, Verify **BUILD SUCCESS**.
Новые тесты проверяют Windows, Mac x64/Rosetta runtime policy, запрет неизвестных
ОС и архитектур, сохранение Java 17 на обеих системах. Это проверка входных
комбинаций policy, не запуск Cocoa SWT на Windows.

Сборка: Maven **3.9.11**, Axiom JDK **17.0.18**, Tycho **4.0.13**.
Target — установленная Windows EDT **2026.1.3+25**, только чтение.
Все runtime-проверки выполняются в `.work/edt-test` и `.work/workspace-probe`.
Bundle **0.1.5.202610141059**, **30 classes major 61**. P2 ZIP содержит только
собственные bundle/feature, обе палитры и редактор палитры, без test fragment.
Лог: `.work/mac-verify.log`; unit reports — `tests/unit/target/surefire-reports`.

Архив: `dist/edt-themes-0.1.5.zip`, **89 961 bytes**.
SHA-256: `761f2208ac009b6582385ecb88c26b8d46b4458dbe40ef34a98e941af352116c`.
ZIP 0.1.4: SHA-256 `43ef21a82b6edd02f9b13131c56b6fba354e464bf71f11cd3754512d69be010d`.

Runtime-регрессия на финальном ZIP: **252 Palette PASS**, **14 PaletteRestart PASS**,
**0 FAIL**, оба JVM завершились exit 0. Проверены 17 ролей, реальные Apply/OK/Cancel,
изолированные drafts Darcula/Monokai, reset, BSL=false, ручные цвета, originals,
сохранение после restart и личный RGB реального BSL keyword glyph.
Тестовые overrides после проверки очищены, журнал цветов пуст.

Сравнение вложенных bundle JAR из ZIP 0.1.4/0.1.5 подтвердило **34 неизменённых
существующих записи**, включая классы палитры/UI/runtime и ресурсы. Отличаются
только EdtCompatibility.class и release metadata; добавлена RuntimePlatform.class.
Лог: `.work/mac-bundle-comparison.txt`. Исходники тестового probe не изменялись.
Сохранённые assertions: [Palette](0.1.5/runtime-palette.txt),
[PaletteRestart](0.1.5/runtime-paletterestart.txt).

Windows p2 Install/Uninstall/Audit: **exit 0**; test fragment удалён через Reconcile.
Собственные bundle/feature отсутствуют в конфигурации копии, активный профиль,
текущие журналы и тестовые personal overrides очищены. Все **1267 базовых ID/версий**
повторно совпали с установленной EDT. SHA-256 нового ZIP проверена после runtime;
все прежние ZIP совпадают со своими сохранёнными SHA-256.
Логи: `.work/mac-install.log`, `.work/mac-reconcile.log`, `.work/mac-uninstall.log`,
`.work/mac-audit.log`. Работа выполнена самостоятельно, без Git/субагентов;
рабочая EDT и глобальная среда не менялись, ПО не устанавливалось.

## Матрица для коллеги с Mac

Сначала зафиксировать macOS version, модель Intel/Apple Silicon, фактические
EDT/Java versions и JVM architecture. Использовать отдельную установку EDT
2026.1.3 и чистую Mac workspace, не переносить Windows originals/journals.
Установить `edt-themes-0.1.5.zip` через Help → Install New Software и перезапустить EDT.
При несовпадении bundle versions сохранить сообщение ошибки; не обходить guard.

| Сценарий | Ожидаемый результат | Статус |
|---|---|---|
| Общая работа версии 0.1.5 на Mac | Плагин работает после изменения runtime policy | Подтверждено пользователем 14.10.2026: «проверено, работает» |
| Установка и открытие «Темы EDT» | Нет отказа Windows-only; страница и 17 ролей доступны | Не подтверждено отдельно |
| Darcula/Monokai и BSL | Цвета применяются, новые editors/окна наследуют выбор | Не подтверждено отдельно |
| Списки темы/шрифта | Имена читаются, клик/Enter/Escape, focus и закрытие popup работают | Не подтверждено отдельно |
| Палитра, HEX и ColorDialog | Preview локальный до Apply; понятные targets; неверный HEX блокирует Apply | Не подтверждено отдельно |
| Личные цвета обеих палитр | Независимы; Apply/OK/Cancel и restart сохраняют правильные значения | Не подтверждено отдельно |
| Сброс роли/стиля | Возврат встроенных цветов, остальные личные/ручные значения сохраняются | Не подтверждено отдельно |
| BSL выключен | Исходные BSL values сохраняются, личный профиль можно сохранить | Не подтверждено отдельно |
| Шрифт и восстановление | Используются доступные Mac fonts, точный исходный font/absence возвращается | Не подтверждено отдельно |
| Восстановление цветов | Исходный вид возвращается; личные палитры остаются; ручные правки защищены | Не подтверждено отдельно |
| Retina / scaling / второе окно | Текст и подписи помещаются, popup не выходит за экран | Не подтверждено отдельно |
| Удаление после восстановления | Свой bundle/feature удалён, базовая EDT не обновлена | Не подтверждено отдельно |

Поведение формы/MXL/графической схемы/СКД проверять по общей ручной матрице.
Системные title/menu controls macOS не обязаны совпадать с Windows.

Контекст исследования и официальные источники:
[оценка переноса](../research/2026-10-14-macos-support.md).

# Редактор палитры 0.1.4

Основание: согласованный handoff `docs/handoff/2026-10-14-palette-editor.md`
и прямое разрешение пользователя реализовать поставку самостоятельно.
Без Git, субагентов, установки ПО и изменений рабочей EDT.

## Решение

Существующая страница получает прокручиваемый редактор 17 ролей: имя,
образец/picker, HEX, признак личного цвета и пояснение со списком targets
из SettingCatalog. Справа/ниже — локальная иллюстрация интерфейса; образец BSL
и независимый шрифт сохраняются. Ошибочный HEX блокирует Apply/OK.

PaletteDrafts держит отдельные черновики и явно затронутые роли обеих палитр.
Resolver объединяет встроенную палитру с overrides. Persisted overrides —
отдельные ключи preferences плагина, не original snapshot.
SnapshotService применяет набор explicit targets выборочно. Личные цвета
записываются в одной транзакции с цветами EDT: pending before/after охватывают
также personal keys, completed original/lastApplied их не содержат.
Поэтому restore не удаляет личные цвета, а отказ/незавершённая операция
откатывает их вместе с EDT. Старый формат журнала сохраняется.

При выборе штатного оформления используется та же транзакция restore с
дополнительными personal writes. Изменённые варианты обоих профилей сохраняются,
применяется только выбранный. BSL=false исключает explicit BSL targets.
Сброс роли/стиля — явное действие для соответствующих ролей даже при отсутствии
override. Отмена закрывает черновик; Apply начинает новую базу черновиков.

## Шаги и проверки

1. RED: unit сценарии merge/validation/reset/изоляция, selective explicit,
   сохранение personal keys при restore, rollback и recovery.
   GREEN: модель, каталог targets и расширение транзакций. `build.ps1 -Action Test`.
2. Runtime/coordinator и UI: загрузка overrides, общий resolver, редактор,
   локальное превью, валидация, Apply/OK/Cancel. Runtime probe сначала проверяет
   отсутствие редактора на базе 0.1.3, затем новое поведение на 0.1.4.
3. Версия 0.1.4 во всех release файлах. Verify → p2 install в существующую копию →
   probe: палитры/перезапуск/BSL/reset/manual edits и регрессия Preview/Apply/Restore/
   FontApply/FontRestore. Reconcile → uninstall → audit.
4. Самостоятельное ревью: атомарность pending/flush, владение SWT resources,
   запрет перехвата чужих keys, отсутствие vendor изменений. Обновить инструкцию,
   raw assertions и фактическую матрицу; старые ZIP сохранить.

## Статус

1. Модель и транзакции реализованы. RED: отсутствующие API, затем реальные
   failures для recovery personal-conflict, native side effects и pending save.
   GREEN: 51 unit test, включая final journal commit failure и codec roundtrip.
   Уточнение: accent имеет 4 RGB targets, но explicit set содержит ещё flag
   hyperlinkColor.SystemDefault; при BSL=false это 4 ключа, при true — 5.
2. UI и coordinator реализованы. Runtime 0.1.3 RED: отсутствует редактор.
   Первичная 0.1.4 прошла функциональные сценарии, снимок отказался захватывать
   обрезанный контрол. Перезапуск подтвердил обе personal maps; дополнительный
   RED выявил silent truncation длинного HEX. Ограничение длины снято.
3. Итоговая сборка 0.1.4.202610141024: Verify BUILD SUCCESS, 51 tests,
   29 classes major 61; p2 Install exit 0. Layout 26, Palette 252,
   PaletteRestart 14 PASS. Preview 133, Apply 878, Restore 100, FontApply 39,
   FontRestore 9 PASS: всего 1451, 0 FAIL. Reconcile, Uninstall/Audit exit 0;
   текущие журналы пусты, базовые 1267 ID/версий сохранены. Все шаги завершены.
4. Самопроверка автора выполнена без Git/субагентов по прямому запросу.
   Проверены pending/commit/rollback, original vs personal, selective explicit,
   BSL gating, независимость font, владение SWT resources и упаковка.
   Найден дефект ширины owner-drawn ячейки: pixel RED→GREEN, Layout 26/26.
   Подписи всех ролей проверены при текущем DPI. Дополнительных блокирующих
   замечаний в самостоятельном ревью не осталось.

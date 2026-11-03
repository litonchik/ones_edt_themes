# Личные палитры 0.1.4

Дополнение 0.1.16: прежние 17 ролей также управляют 21 цветом QL/DCS
самостоятельных редакторов и текстовых контролов конструктора запроса.
Personal keys и формат журналов не изменились. Selective explicit apply
учитывает новые targets соответствующей роли; флаг BSL исключает только BSL.
[Проверка EDT 2026 и сохранности цветов](../testing/edt-2026-compatibility-validation.md).

Реализация для EDT 2026.1.3+25, 14 октября 2026.
Исходные факты и 17 ролей: `../research/2026-10-14-palette-editor.md`.
Настройки/IDs не добавлялись: используется прежняя проверенная карта 51 RGB
target и пяти SystemDefault flags.

## Состояния

`PaletteDrafts` содержит локальные maps Darcula/Monokai и touched roles.
Все цвета нормализуются в #RRGGBB, неизвестные роли/профили и неверный HEX
отклоняются. Эффективная Palette — ресурсы PaletteCatalog плюс overrides.
Совпадение со встроенным цветом удаляет override, но сохраняет explicit intent.
Reset также отмечает роли явно затронутыми. Accept очищает intent только после
успешного сохранения. Ошибочные строки HEX хранятся только в экземпляре редактора,
сохраняются при переключениях и блокируют Apply/OK всего диалога.

`instance/ru.litonchik.edt.themes` хранит ключи `palette.<profile>.<role>`.
Отсутствующий ключ означает встроенный цвет. Чтение старой workspace без этих
ключей даёт исходные палитры. Ни restore, ни журнал независимого шрифта не
включают personal keys в оригиналы. Native/force restore сохраняет личные цвета.

## Атомарное применение

ThemeCoordinator принимает PaletteDrafts, формирует обычный план EDT и отдельную
map personal writes для touched roles обоих профилей. SettingCatalog.roleTargets
выдаёт explicit keys выбранной палитры, включая соответствующие SystemDefault.
При BSL=false BSL targets исключены из explicit; прежняя защита originals BSL
остаётся. Для остальных targets действует прежний lastApplied/surrendered.

SnapshotService.apply(desired,explicit,personal) строит одну транзакцию. Personal
keys присутствуют только в pendingBefore/pendingAfter. Completed snapshot не
содержит их в original/lastApplied/surrendered. Формат SnapshotCodec version=1
уже поддерживал pending entry без original; кодек не изменён. Two-slot store
сначала flush journal, потом переключает pointer. При ошибке write/flush/final
commit откатываются EDT и personal writes; при неудаче rollback pending journal
остаётся доступен для recovery. Конфликт personal при recovery сохраняет текущее
внешнее значение и не превращается в managed original.
Если журнал ещё pending, попытка сохранить personal colors через restore
отклоняется: recovery не выдаётся за успешное подтверждение новых черновиков.
Сначала следует выполнить восстановление журнала, затем повторить Apply.

Выбор штатного оформления использует restore с той же map personal writes.
Это атомарно возвращает EDT и сохраняет черновики профилей. Неполное применение
не принимается в UI. Шрифт подтверждается отдельным независимым действием, как
и раньше; ошибка шрифта не откатывает уже успешно подтверждённую палитру.

Surrendered colors повторно записываются после theme setter в текущем значении,
чтобы побочный эффект штатной CSS темы не затёр внешнюю правку. Они остаются
неуправляемыми; явная правка только соответствующей роли возвращает владение.

## UI и границы

PaletteEditor — таблица 17 ролей со swatch/HEX/маркером, ColorSelector,
HEX Text, два сброса, пояснение выбранной роли и tooltip targets из карты.
Фиксированный Canvas иллюстрирует интерфейсные роли. CodePreview и ReadableCombo
используют тот же resolved draft. До Apply/OK нет записи настроек.
SWT Color/Font принадлежат странице; ColorSelector освобождает своё изображение
вместе с кнопкой. Нативные цвета Windows не меняются.

Страница прокручивается, небольшое окно не скрывает настройки без возможности
доступа. Иллюстрация не является аудитом эффективных project/native colors.
Содержимое формы/MXL/схемы/СКД не включено в редактор. Hover строк отложен.

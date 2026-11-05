# Noctis Lilac: адаптация 17 ролей

Дата: **04.11.2026**. Добавляется одна схема: **Noctis Lilac**,
`noctis-lilac`, группа LIGHT. Это восьмая светлая палитра и последний вариант
текущей очереди: основные пары из каталога и три светлых Noctis завершены.
Dracula исключена пользователем; дополнительные GitHub/2017/Unbordered
и самостоятельные IntelliJ Light/One Light в эту очередь не входят.
Общий механизм 0.1.25 и прежние 18 палитр сохраняются.

Источник — Noctis **10.43.3**, commit `4a82370b2c064e36726c23e94f27ed3782bd6ecd`:
[manifest](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/package.json),
[Lilac JSON](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/themes/lilac.json),
[MIT](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/LICENSE.md).
Источники прочитаны 04.11.2026. MIT побайтово совпал с `noctis-LICENSE.txt`;
отдельный дубликат не добавляется. Manifest подтверждает uiTheme=vs.
[SHA-256 файлов, привязки и расчёты](noctis-lilac-source-mapping.json).

| Роль | RGB | Основание |
|---|---|---|
| editor.background | #F2F1F8 | editor.background |
| text | #0C006B | editor.foreground / TEXT |
| panel | #EDECF8 | sideBar.background |
| border | #D4D0F1 | editorGroup.border |
| selection.background | #BCB6E7 | list.activeSelectionBackground; same role policy as Lux/Hibernus |
| selection.text | #0C006B | editor.foreground |
| line | #E1DEF3 | editor.lineHighlightBackground #D5D2EF99 composited over #F2F1F8 |
| comment | #615E74 | COMMENT #9995B7 darkened |
| keyword | #A6395F | KEYWORD #FF5792 darkened |
| string | #006F41 | STRING #00B368 darkened |
| number | #543FF2 | NUMBER #5842FF darkened |
| directive | #0064A3 | MISC #0094F0 darkened; BSL adaptation |
| accent | #006B79 | textLink.foreground #00C6E0 darkened |
| error | #C73200 | errorForeground #FF4000 darkened |
| warning | #935F21 | editorWarning.foreground #E69533 darkened |
| disabled | #615E74 | adapted COMMENT for shared disabled/line number role |
| hover | #DEDBF5 | editorHoverWidget.background |

Редактор #F2F1F8, панели #EDECF8 и hover #DEDBF5 сохранены исходными.
Прозрачный current-line #D5D2EF99 сведён поверх editor по sRGB-каналам,
8-bit alpha с округлением до ближайшего целого: #E1DEF3.
Исходный editor.selectionBackground #B4ADEB77 даёт #D5D1F2. Для общей роли,
включая выбранные строки UI, используется непрозрачный list selection #BCB6E7
по тому же правилу, что у Lux/Hibernus. Контраст selection/line — 1.452:1,
выбранного текста #0C006B — 8.94:1. Это адаптация, не нормативный порог UI.

COMMENT/KEYWORD/STRING/NUMBER/MISC/link затемнены общим коэффициентом
для трёх sRGB-каналов до 4.6:1 на editor/line/hover; error/warning — на panel.
В Lilac затемнён также NUMBER: исходный фиолетовый недостаточно контрастен
на hover. Основной тёмно-синий text #0C006B остаётся исходным.
Соотношения каналов сохраняются приблизительно с учётом округления.
Disabled использует адаптированный COMMENT вместо светлых номеров источника.
Directive=MISC — адаптация к BSL по примеру прочих Noctis, не upstream BSL scope.
Native BSL font styles сохранены; отдельные ITALIC/BOLD не переносятся.
Расчёт контраста не означает полной проверки доступности EDT.

Фактическая проверка — [отчёт 0.1.26](../testing/noctis-lilac-validation.md).

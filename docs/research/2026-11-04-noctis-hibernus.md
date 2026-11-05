# Noctis Hibernus: адаптация 17 ролей

Дата: **04.11.2026**. Добавляется одна схема: **Noctis Hibernus**,
`noctis-hibernus`, группа LIGHT. Это седьмая светлая палитра; основные светлые
пары первоначального списка уже реализованы, продолжаем официальные варианты
семейства Noctis. Lilac и светлая Dracula в этот этап не входят.
Общий механизм 0.1.24 и прежние 17 палитр сохраняются.

Источник — Noctis **10.43.3**, commit `4a82370b2c064e36726c23e94f27ed3782bd6ecd`:
[manifest](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/package.json),
[Hibernus JSON](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/themes/hibernus.json),
[MIT](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/LICENSE.md).
Источники прочитаны 04.11.2026. MIT побайтово совпал с уже включённой
`noctis-LICENSE.txt`; отдельный дубликат не добавляется.
[SHA-256 файлов, привязки и расчёты](noctis-hibernus-source-mapping.json).

| Роль | RGB | Основание |
|---|---|---|
| editor.background | #F4F6F6 | editor.background |
| text | #005661 | editor.foreground |
| panel | #E7F2F3 | sideBar.background |
| border | #D0EDF1 | editorGroup.border |
| selection.background | #B6E1E7 | list.activeSelectionBackground; distinction from current line |
| selection.text | #005661 | editor.foreground |
| line | #D8EDF0 | editor.lineHighlightBackground #D1EBEFCC composited over #F4F6F6 |
| comment | #5A6B6B | COMMENT #8CA6A6 darkened |
| keyword | #B23D66 | KEYWORD #FF5792 darkened |
| string | #007745 | STRING #00B368 darkened |
| number | #5842FF | NUMBER #5842FF unchanged |
| directive | #006CAE | MISC #0094F0 darkened; BSL adaptation |
| accent | #007282 | textLink.foreground #00C6E0 darkened |
| error | #CA3300 | errorForeground #FF4000 darkened |
| warning | #956021 | editorWarning.foreground #E69533 darkened |
| disabled | #5A6B6B | adapted COMMENT for shared disabled/line number role |
| hover | #E0EFF1 | editorHoverWidget.background |

Фон Hibernus холодный серый, панели голубоватые; Lux остаётся отдельной
кремовой темой. Прозрачный current-line #D1EBEFCC сведён поверх #F4F6F6
по sRGB-каналам с 8-bit alpha и округлением до ближайшего целого: #D8EDF0.
Исходное editor.selectionBackground #ADE2EB77 даёт #D3EDF1, близкий текущей
строке. Как у Lux, выбран исходный непрозрачный list selection #B6E1E7:
1.160:1 относительно line; основной выбранный текст — 5.96:1.
Это адаптация различимости, а не нормативный порог для интерфейса.

COMMENT/KEYWORD/STRING/MISC/link и error/warning затемнены равномерным
масштабированием трёх sRGB-каналов до 4.6:1: текст на editor/line/hover,
error/warning на panel. Основной текст и NUMBER сохранены исходными.
Соотношения каналов сохраняются приблизительно с учётом округления.
Disabled использует адаптированный COMMENT вместо светлых номеров источника.
Directive=MISC — адаптация к BSL по примеру Minimus/Lux, не upstream BSL scope.
Общий текст берётся из editor.foreground, не из узкого TEXT token scope.
Native BSL font styles сохранены; отдельные ITALIC/BOLD не переносятся.
Расчёт контраста не означает полной проверки доступности EDT.

Фактическая проверка — [отчёт 0.1.25](../testing/noctis-hibernus-validation.md).

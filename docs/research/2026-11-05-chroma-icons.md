# Значки v8-chroma для EDT

Исследование: 05.11.2026. Источник —
[APonkratov/v8-chroma](https://github.com/APonkratov/v8-chroma),
фиксированный коммит `5105ccf35ae63122a2a98c98230bebfd4ef849af`.
Пользователь сообщил, что согласовал использование с автором.

## Подтверждённые источники

- [Собственные SVG и устройство набора](https://github.com/APonkratov/v8-chroma/blob/5105ccf35ae63122a2a98c98230bebfd4ef849af/docs/custom-icons.md).
- [MIT](https://github.com/APonkratov/v8-chroma/blob/5105ccf35ae63122a2a98c98230bebfd4ef849af/LICENSE),
  Copyright (c) 2026 Aleksandr Ponkratov and v8-chroma contributors.
- SVG-мастера находятся в assets/toolbar-icons/light и dark. Для первого
  этапа взяты 49 рисунков метаданных на каждый набор. Все 98 файлов сверены
  с Git blob SHA закреплённой ревизии; SHA-256 сохранены в source.json.
- PNG получены из SVG функцией raster из upstream scripts/icon_raster.py.
  Набор самодостаточен: для генерации нужны Python и уже доступный Pillow,
  установленная 1С/Cairo и нативный движок v8-chroma не используются.

Не заимствуются оригинальные bitmap платформы, таблицы их отпечатков,
нативные перехваты и DLL. Используются собственные SVG и MIT-растеризатор.

## Точки интеграции EDT

В установленной EDT 2026.1.3 проверены plugin.xml и ресурсы bundles:
com._1c.g5.v8.dt.navigator.ui, org.eclipse.ui.navigator и
com._1c.g5.v8.dt.md.ui.shared. NavigatorDecoratingLabelProvider получает
публичный workbench IDecoratorManager.getLabelDecorator().
Исследован org.eclipse.ui.decorators: фактически lightweight-отметки
добавляются перед heavyweight-декораторами. Поэтому выбран публичный
DecoratingStyledCellLabelProvider.setLabelDecorator на том же native
label provider Навигатора. Обёртка подставляет основу перед вызовом исходного
декоратора; глобальные реестры и контексты не меняются.
Схема enablement этого extension point — legacy ActionExpression с
objectClass, а не core-expressions instanceof; это сверено с EGit.

[ILabelDecorator](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/viewers/ILabelDecorator.html),
[DecoratingLabelProvider](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/viewers/DecoratingLabelProvider.html).
ICommandImageService предоставляет чтение, но не публичную привязку новых
изображений команд. Подмена командных панелей и меню в этот этап не входит.
Для lightweight IDecoration.REPLACE требуется opt-in в контексте клиента;
глобальный DecorationContext не изменяется.

## Решения первого этапа

- Необязательный флаг на странице «Темы EDT», по умолчанию выключенный.
- Светлый/тёмный набор выбирается по фактической основе интерфейса.
- 64 имени штатных metadata PNG сопоставлены 49 рисункам. Карта основана
  на реально найденных icons/obj16 установленного md.ui.shared.
- Замена разрешается только при точном совпадении размеров и нормализованных
  RGBA с известной базовой картинкой установленной EDT. Неоднозначные
  совпадения, неизвестные/изменённые/составные изображения пропускаются.
- Отключение возвращает стандартную картинку label provider. Исходные Image,
  ImageRegistry, текст узла и поведение команд не меняются.
- Растры подготовлены для 16/20/24/28/32/48/64 px, то есть 100–400%.

Фактическая проверка и ограничения —
[chroma-icons-validation.md](../testing/chroma-icons-validation.md).
